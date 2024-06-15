# SELEN VOIP Setup

## Server Setup
- AWS Lightsail or EC2
- VPN Server 
	- Ubuntu 22.04 LTS
	- 2 GB RAM, 2 vCPUs, 60 GB SSD
	- Static IP
	- Assign domain (vpn.selen.click) -- [optional]
	- Firewall (IPV4 & IPV6)
		
		Service|Protocal|Port|Destination|
		-------|--------|----|-----------|
		Ping (ICMP)	|ICMP	 |		|Any IPv4 address
		SH				|TCP	 |22 	|Any IPv4 address
		HTTP			|TCP	 |80	|Any IPv4 address
		HTTPS			|TCP	 |443	|Any IPv4 address
		Custom			|UDP	 |1194	|Any IPv4 address
		
- VOIP Server 
	- Debian 12
	- 2 GB RAM, 2 vCPUs, 60 GB SSD
	- Static IP
	- Assign domain (voip.selen.click) -- [optional]
	- Firewall (IPV4 & IPV6) [reference](https://docs.fusionpbx.com/en/latest/firewall.html)
		
		Service|Protocal|Port|Destination|
		-------|--------|----|-----------|
		Ping (ICMP)	|ICMP	| 		|Any IPv4 address
		SSH				|TCP	|22		|Any IPv4 address
		DNS (TCP)		|TCP	|53		|Any IPv4 address
		HTTP			|TCP	|80		|Any IPv4 address
		HTTPS			|TCP	|443	|Any IPv4 address
		Custom			|TCP	|5060 - 5091	|Any IPv4 address
		DNS (UDP)		|UDP	|53		|Any IPv4 address
		Custom			|UDP	|5060 - 5091	|Any IPv4 address
		Custom			|UDP	|16384 - 32768 |Any IPv4 address



## Installation

### VPN
- ssh into server and drop to root.
- Install using [script](https://github.com/angristan/openvpn-install)

	```sh
	curl -O https://raw.githubusercontent.com/angristan/openvpn-install/master/openvpn-install.sh

	chmod +x openvpn-install.sh
	```
- Enter VPN public IP when prompted.
- Create & download profile (.ovpn file)

### VOIP
- ssh into server and drop to root.
- Install using [script](https://docs.fusionpbx.com/en/latest/getting_started/quick_install.html)

	```sh
	wget -O - https://raw.githubusercontent.com/fusionpbx/fusionpbx-install.sh/master/debian/pre-install.sh | sh;
	
	cd /usr/src/fusionpbx-install.sh/debian && ./install.sh

	```
- Take note of credentials onscreen after complete.
- Whitelist VPN IP
	- add VPN public IP and VOIP public IP to `/etc/fail2ban/jail.conf`
	```
	ignoreip = 127.0.0.1/8 18.136.85.22/32 52.74.86.196/32 ::1
	```

## FUSIONPBX Settings
- First time login using onscreen credentials (admin@172.x.x.x)
- Change superadmin password
- Add public IP to server (Advanced --> Domains --> add public IP)
- Create superadmin for this public IP domain.
- Add FQDN domain
- Create superadmin for this FQDN
- Login with new superadmin and create extensions

| Extension | Effective CID Name | Outbound Name            | Outbound Number | Call Group | Address     | Description       |
|-----------|--------------------|--------------------------|-----------------|------------|-------------|-------------------|
| 1111      | Call Center 1111   | WHA Call Center - 1111   | 02338388        | callCenter | 52.74.86.196 | Call Center 1111  |
| 2222      | Call Center 2222   | WHA Call Center - 2222   | 02338388        | callCenter | 52.74.86.196 | Call Center 2222  |
| 3333      | Call Center 3333   | WHA Call Center - 3333   | 02338389        | callCenter | 52.74.86.196 | Call Center 3333  |
| 4444      | Alarm Box 4444     | WHA Alarm - 4444         | 02334444        | alarmbox   | 52.74.86.196 | Alarm Box 4444    |
| 5555      | Alarm Box 5555     | WHA Alarm - 5555         | 02335555        | alarmbox   | 52.74.86.196 | Alarm Box 5555    |
| 6666      | Alarm Box 6666     | WHA Alarm - 6666         | 023386666       | alarmbox   | 52.74.86.196 | Alarm Box 6666    |
| 7777      | Alarm Box 7777     | WHA Alarm - 7777         | 023387777       | alarmbox   | 52.74.86.196 | Alarm Box 7777    |
| 8888      | Alarm Box 8888     | WHA Alarm - 8888         | 023388888       | alarmbox   | 52.74.86.196 | Alarm Box 8888    |
| 9999      | Alarm Box 9999     | WHA Alarm - 9999         | 02338321        | alarmbox   | 52.74.86.196 | Alarm Box 9999    |

- **Edit SIP Profiles**
	- External
	
	```
	ext-rtp-ip	autonat:52.74.86.196  	True
	ext-sip-ip	autonat:52.74.86.196  	True
	rtp-ip 		52.74.86.196  			True
	sip-ip 		52.74.86.196  			True
	```	 
	
	- Internal

	```
	ext-rtp-ip	52.74.86.196  	True
	ext-sip-ip	52.74.86.196  	True	 
  	```
  	
  	- Restart both profiles in Status --> SIP Status
 
 - Upload recording WAV file inside Applications --> Recordings
 - Create Ring Group
 
	 	| Name        | Extension | Strategy   | Forwarding | Enabled | Description             |
		|-------------|-----------|------------|------------|---------|-------------------------|
		| Call Centers| 9000      | Enterprise |            |         | Call Centers Ring Group |

		| Destination | Delay | Timeout |
		|-------------|-------|---------|
		| 1111        | 0     | 30      |
		| 2222        | 0     | 30      |
		| 3333        | 0     | 30      |
		| 4444        | 0     | 30      |
		| 5555        | 0     | 30      |

	- Create IVR Menu
	
		| Name      | Extension |
		|-----------|-----------|
		| Main IVR  | 0         |
		

- Drop previous registration
    - SIP settings values
        [ref settings](https://developer.signalwire.com/freeswitch/FreeSWITCH-Explained/Configuration/Sofia-SIP-Stack/Sofia-Configuration-Files_7144453/)

        [ref config](https://freeswitch.org/confluence/display/FREESWITCH/Sofia+Configuration+Files)

    - multiple-registrations
        - Valid values for this parameter are "contact", "true", "false". value="true" is the most common use. Setting this value to "contact" will remove the old registration based on sip_user, sip_host and contact field as opposed to the call_id.

        `<param name="multiple-registrations" value="contact"/>` --> If re-register, drop current one

## Android Code
