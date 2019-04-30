def get_required_entries(legacy: bool):
    fileName = "entries-legacy.txt" if legacy else "entries.txt"
    entries = set([line.strip(' "\t\r\n') for line in open(fileName)])
    return entries


def json_to_shopchest(required_entries: set):
    fileIn = input("JSON Input File: ")
    fileOut = input("Output File: ")

    with open(fileOut, "w", encoding="utf-8") as out:
        for line in open(fileIn, "r"):
            new_line = line.strip(' "\t\r\n') # Trim whitespace (start and end)
            new_line = new_line.replace('": "', "=") # Replace ": " with = (middle part)
            new_line = new_line.replace('",', "") # Delete ", (end part)
            new_line = new_line.encode("utf-8").decode("unicode-escape") # Decode unicode characters
            
            prop_name = new_line.split("=")[0]
            if prop_name in required_entries:
                out.write(new_line + "\n")
                required_entries.remove(prop_name)

    if len(required_entries) > 0:
        print()
        print(str(len(required_entries)) + " required entries missing from input file:")

        for prop_name in required_entries:
            print("- " + prop_name)


def lang_to_shopchest(required_entries: set):
    fileIn = input("Input File: ")
    fileOut = input("Output File: ")

    with open(fileOut, "w", encoding="utf-8") as out:
        for line in open(fileIn, encoding="utf-8"):
            new_line = line.strip(' "\t\r\n') # Trim whitespace (start and end)
            prop_name = new_line.split("=")[0]
            if prop_name in required_entries:
                out.write(new_line + "\n")
                required_entries.remove(prop_name)

    if len(required_entries) > 0:
        print()
        print(str(len(required_entries)) + " required entries missing from input file:")

        for prop_name in required_entries:
            print("- " + prop_name)


legacy = input("Pre 1.13? [y/n]: ") == 'y'
required_entries = get_required_entries(legacy)

print()
print("Legacy: " + str(legacy))
print(str(len(required_entries)) + " entries required")
print()

if legacy:
    lang_to_shopchest(required_entries)
else:
    json_to_shopchest(required_entries)