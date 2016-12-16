# LinkMe
A simple Minecraft Sponge link-sharing Plugin

## Creating Links
Edit the `linkme.conf` file located in `~/config/linkme.conf` it should look something like the following:

    links {
        Hello = "Hello World!"
        Link = "https://www.spongepowered.org"
        Formatted = "&9I'm Blue"
    }
  
The format of links are `<Link Name> = <Link Message>`    
**Important:** If you want links in your messages to be clickable they *must* start with either `http://` or `https://`

To use a link, players will simply execute `/<Link Name>` in chat and they will recieve the link message.  
**Important:** All link commands will be assigned a permission node with the value `linkme.commands.<lowercase link name>` where `<lowercase link name>` is the name of the link **in all lowercase letters**!
This will allow you to control which links are available to each player.
