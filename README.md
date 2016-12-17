# LinkMe
A simple Minecraft Sponge link-sharing Plugin

##The Idea
The general permise behind LinkMe is simple, when running a server you often times have links you want to share with your players (website, forums, ect.) however there may be too many to embed in the motd, so you'll inevitably have players asking "What is the link to ***x*** resource?" That's where LinkMe steps in! LinkMe will allow you to create commands, `/forum` for exampe, that will send them a link to the appropriate site.  LinkMe can also be used to send simple messages to the player as well such as rules, help messages, ect.

## Creating Links
Edit the `linkme.conf` file located at `~/config/linkme.conf` it should look something like the following:

    links {
        Hello = "Hello World!"
        Link = "https://www.spongepowered.org"
        Formatted = "&9I'm Blue"
    }
  
The format of links are `<Link Name> = <Link Message>`    
**Important:** If you want links in your messages to be clickable they *must* start with either `http://` or `https://`

To use a link, players will simply execute `/<Link Name>` in chat and they will recieve the link message.  
**Important:** All link commands will be assigned a permission node with the value `linkme.commands.<lowercase link name>` where `<lowercase link name>` is the name of the link **in all lowercase letters**! This will allow you to control which links are available to each player.
