import os
from os.path import isdir, isfile, join
from shutil import copy2


def clearTarget():
    files = os.listdir("target")
    print(files)
    for file in files:
        if file.endswith(".jar"):
            os.remove(join("target", file))


def findJar(directory: str):
    files = os.listdir(directory)
    for file in files:
        path = join(directory, file)
        if isfile(path) and file.endswith(".jar"):

            if file.endswith("-javadoc.jar"): continue
            if file.endswith("-sources.jar"): continue
            if file.startswith("original-"): continue

            return path
    return None


def findTargetDirs():
    result = [
        # join("api", "target"),
        join("core", "target"),
    ]

    directories = os.listdir("addons")
    for directory in directories:
        path = join("addons", directory)
        if isdir(path):
            result.append(join(path, "target"))

    return result


try:
    clearTarget()
except FileNotFoundError:
    pass

targetDirs = findTargetDirs()

for targetDir in targetDirs:
    jarFile = findJar(targetDir)
    if jarFile != None:
        fileName = jarFile.split("\\")[-1]
        
        try:
            os.mkdir("target")
        except FileExistsError:
            pass
        
        copy2(jarFile, join("target", fileName))