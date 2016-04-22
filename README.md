# Voxel Maven
Voxel: A contraction of the words "volume" and "element" and represents a value on a regular grid in three-dimensional space.

Maven: An expert; one who understands (also coincidentally the a Java build automation tool this project uses) 

Voxel maven programmatically generates 3d models and can export to STL meshes. Due to the nature of voxels, models that are difficult to generate in a mesh context, can be easier to generate in a voxel context. 

##How to Install
```
git clone https://github.com/AbFab3D/AbFab3D.git
cd AbFab3D
ant build
mvn install:install-file -Dfile=jars/abfab3d_core_1.0.0.jar -DgroupId=abfab3d -DartifactId=abfab3d -Dversion=1 -Dpackaging=jar
cd ..
git clone https://github.com/rcpedersen/voxelmaven.git
cd voxelmaven
mvn install 
```
##Getting started
Please see the [wiki](https://github.com/rcpedersen/voxelmaven/wiki/1: Getting-Started) for more help on making your first project using voxelmaven. 
##Credits
Voxelmaven depends on the fabulous AbFab3D library for reading STL files, converting Voxels into triangles, and exporting meshes to STL files. 
