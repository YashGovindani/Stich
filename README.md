<img src="needle.png" width="180px" align="right"/>

# Stitch   
Stitch is a J2EE web services framework.  
Stitch makes backend programming easier.   

## Usage   

### Setting up Stitch   
> Note : In steps below, it is assumed that user is creating a new web application and using linux operating system.   

**Step 1 :** Go to tomcat's webapps folder -> ```path_to_tomcat_Directory/webapps```    
**Step 2 :** Create a new Directory -> ```mkdir webapp_name```    
**Step 3 :** Change directory to your app directory -> ```cd webapp_name```    
**Step 4 :** Complete the folder structure as follow :
```
mkdir WEB-INF && cd WEB-INF
mkdir classes
mkdir lib
mkdir secured
```
**Step 5 :** Either copy classes or jar    
**For classes :**    
```
git clone https://github.com/YashGovindani/Stitch.git
cp -R Stitch/src/com classes/.
cp Stitch/dependencies/* lib/.
cp Stitch/web.xml .
rm -R Stitch
```
**For jar**
```
git clone https://github.com/YashGovindani/Stitch.git
cp Stitch/lib/* lib/.
cp Stitch/dependencies/* lib/.
cp Stitch/web.xml .
rm -R Stitch
```
**Step 6 :** Set following init-param(s) in web.xml as per requirement
- **SERVICE_PACKAGE_PREFIX :** Defines the prefix of the package wherein the services could be found.    
- **REQUEST_PARAMETER :** Defines the request parameter for secured access to client-side technology.    

    

### #1 Basic Scenario
Let's consider URL to hit is


> Note : At present stage, The framework only works with [Tomcat](https://tomcat.apache.org/)
