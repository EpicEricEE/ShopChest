# Language Script

A script to convert Minecraft language files to ShopChest language files. <br>
The plugin's messages need to be added manually.

## Usage

To get a Minecraft language file (that is not `en_us`), follow these steps:

1. Get in your `.minecraft/assets/indexes` folder and open the file that is named the desired version.

2. Search for your language code (e.g. `de_de` or `fr_fr`). There should be 2 results: One for the realms language file and one for the normal language file.

3. Copy the `hash` code and search for it in the `.minecraft/assets` folder. The hash code is the filename of the language file.

Once done, copy the language file into a working directory and launch `lang.py` (via Python3). The output file will be encoded in `UTF-8` (line endings: LF).<br>
Now add any missing entries (e.g. by checking other language files) and copy the plugin's messages.

Don't forget to append `-legacy` to the file name when dealing with version below 1.13.