# LinkMe - Share Links & Info with Players via simple Commands!

## The Idea
The general premise behind LinkMe is simple: when running a server, you often have links you want to share with your players (website, forums, etc.). However, there may be too many to embed in the motd, so you'll inevitably have players asking "What is the link to ***x*** resource?" That's where LinkMe steps in! LinkMe will allow you to create commands, `/forum` for example, that will send them a link to the appropriate site.  LinkMe can also be used to send simple messages to the player, such as rules, help messages, etc.

## Creating Links
Edit the `linkme.conf` file located at `~/config/linkme.conf` it should look something like the following:

    links {
      Hello = "Hello World!"
      Link = "https://www.spongepowered.org"
      Formatted = "&9I'm Blue"
    }
  
The format of links are `<Link Name> = <Link Message>`    
**Important:** If you want links in your messages to be clickable they *must* start with either `http://` or `https://`

To use a link, players will simply execute `/<Link Name>` in chat and they will receive the link message.  
**Important:** All link commands will be assigned a permission node with the value `linkme.commands.<lowercase link name>` where `<lowercase link name>` is the name of the link **in all lowercase letters**! This will allow you to control which links are available to each player.

## Advanced Mode - HOCON Links
For those of you with configuration ninja skills links can also be specified in [HOCON Configuration Format](https://docs.spongepowered.org/master/en/plugin/text/representations/configurate.html). For example:
    
    links {
      HoconLinkExample {
        clickEvent {
          action = "open_url"
          value = "https://www.spongepowered.org"
        }
        color = yellow
        text = Sponge
        underlined = true
      }
    }

This would create a link with the name `HoconLinkExample` which would display *Sponge* underlined, in yellow, and would link to the Sponge homepage.


## Support Me
I will **never** charge money for the use of my plugins, however they do require a significant amount of work to maintain and update. If you'd like to show your support and buy me a cup of tea sometime (I don't drink that horrid coffee stuff :P) you can do so [here](https://www.paypal.me/zerthick)
