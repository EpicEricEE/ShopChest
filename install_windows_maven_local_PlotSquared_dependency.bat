
CALL mvn install:install-file  -Dfile=%CD%/lib/PlotSquared-Bukkit-4.4.495.jar ^
                          -DgroupId=com.plotsquared ^
                          -DartifactId=PlotSquared ^
                          -Dversion=5.1 ^
                          -Dpackaging=jar ^
                          -DgeneratePom=true
						  
pause