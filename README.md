# etester-parent - Uses JDK 17
### This is how it transpired....
* Jakarta JAXB is only Supported in Spring 6.  
* BTW, I am using jakarta.xml.bind:jakarta.xml.bind-api:4.0.0 AND org.glassfish.jaxb:jaxb-runtime:4.0.1.
* Spring 6 is only in spring-boot/3.0.0/.   (Boot Version 2.7.5 uses Spring 5)
* Finally, spring-boot 3 only supports JDK 17 and above
* Here is teh comment and post that got me down this path.  "I saw that Spring 5 doesn't support Jakarta, they planned to add support in future 6 version."
* https://stackoverflow.com/questions/72667804/no-adapter-for-endpoint-spring-ws-soap

