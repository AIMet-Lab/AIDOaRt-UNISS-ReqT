# ReqT

[![license](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE.md)

ReqT is an open-source tool for the automatic testing of reactive black-box systems. 
It uses a formal specification to chose which action to perform on a system and to
evaluate its response.

## Attribution

ReqT is open source software released under the [GPLv3 license](LICENSE.md).



## Build

In order to build ReqT, you need to first initialize the environment

    git submodule update --init --recursive

Now you can build a new distribution of the software simply running in the project dir the following command:
   
    ./gradlew build
      
It will automatically build a .zip and a .tar files in the `build/distributions` directory.
To run ReqT simply decompress one of the two files and execute the command
   
    ./bin/ReqT
    
in the folder `examples` you can find a set of examples (specification and SUT) to try the application.

### Dependencies

ReqT has two external dependencies:
- [Spot](https://spot.lrde.epita.fr/) 2.7+
- Java 11+


## Documentation
To learn more about ReqT you can read the [tutorial](docs/TUTORIAL.md) and download the [brochure](docs/brochure.pdf).
