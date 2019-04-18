# Running Liberty Bikes at a Conference

The following information can be used to setup the Liberty Bikes game on a standalone laptop and local wifi router for the purposes of demoing it at a conference. 


# Hardware needed:

1. Laptop with Liberty Bikes code
2. Ethernet cable
3. Wifi Router (ex, Linksys)
4. Up to 4 mobile devices (one for each player)


# Router setup (linksys)

1. Configure the router to have an IP address of 192.168.1.1
2. Enable DHCP with starting address of 192.168.1.100
3. Ensure the DHCP can issue at least 5 IP addresses for the 4 players (given that you have 4 designated devices vs users walking up an using their devices) and the local laptop.  Typically you can just take the default of 50
4. Configure the name of the router to be OpenLibertyBikes


# Laptop setup (Windows 10)

1. Update Ethernet interface to use a static IP address on your laptop:
    1. Control Panel -> Network and Internet -> Network Sharing Center -> Change adapter settings.
    1. Right click adapter and select properties.  Select Internet Protocol V4 and click the properties button.
    1. Set IP address to 192.168.1.100
    1. Set subnet mask to 255.255.255.0
    1. Set default gateway to 192.168.1.1 or gateway ip of router
    1. Set obtain DNS to automatic
    1. Click okay and close the window so that the change takes affect.
    1. You can do ipconfig in a DOS cmd.exe window to make sure the ip address is set for the IPv4 adapter.
        1. If issues, you can do ipconfig /release to release all IP address followed by ipconfig /renew to renew all the IP connections.
1. Plug one end of Ethernet cable into laptop.  
1. Plug other end of Ethernet cable into one of the 4 ports of the router (not the internet port!)
1. Start the game in singleparty mode with: ./gradlew start frontend:open -DsingleParty=true
1. When done, stop the game with ./gradlew stop


# Dedicated Device setup (Android)

1. Connect each of the mobile devices to the to the OpenLibertyBikes router
wifi -> OpenLibertyBikes
2. Enter the link to login and play the game into the browser (we've found that Firefox works for older phones, and Safari works on newer ones) on each of the mobile devices: https://192.168.1.100:12000/login
3. Enter name and select play as a guest

