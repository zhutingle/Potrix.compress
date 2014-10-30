Potrix.compress
===============

Compress html/js/css files while package the project.

When you package a web project use apache-ant.And then you want compress the html/js/css files during package execution.

Maybe you can use this.

Add the compress folder to you web project.
Make the compress.properties right in your projct.
Write a target in the build.xml before war.Like this:

<target name="compress" depends="copy">
	<java fork="false" classname="potrix.compress.Main" failonerror="true">
		<classpath path="${compress}/compiler.jar" />
		<classpath path="${compress}/potrix.compress.jar" />
	</java>
</target>

Then it will compress files that you want.
