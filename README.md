TrustChain Android [![Build Status](https://travis-ci.org/ClintonCao/CS4160-trustchain-android.svg?branch=master)](https://travis-ci.org/ClintonCao/CS4160-trustchain-android) [![codecov](https://codecov.io/gh/ClintonCao/CS4160-trustchain-android/branch/master/graph/badge.svg)](https://codecov.io/gh/ClintonCao/CS4160-trustchain-android)
==================

TrustChain Android is a native Android app implementing the TU Delft style blockchain, called TrustChain. This app provides an accessible way to understand and to use TrustChain. The app is build as part of a Blockchain Engineering course of the TU Delft. It is meant as a basic building block to experiment with blockchain technology. This documentation should get you started in the workings of the app, however for thorough understanding, reading other documentation and looking at the source code is a necessity.

We have tried to make the code clear. However, this app was not build by Android experts so please don't hold any mistakes or weird structures for Android against us. Instead, please let us know what could be improved, or provide a fix yourself by submitting a pull request.

Documentation
=============
The documentation for this project can be found at [ReadTheDocs](http://trustchain-android.readthedocs.org).


Overview of the app
====================
![alt text](https://github.com/ClintonCao/CS4160-trustchain-android/blob/master/docs/source/images/overview_app.png)


UML diagram for code that is responsible for the connection between peers
==========================================================================
![alt text](https://github.com/ClintonCao/CS4160-trustchain-android/blob/master/docs/source/images/uml_diagram_connection.png)


Test Coverage Report
==========================================================================
![alt text](https://github.com/ClintonCao/CS4160-trustchain-android/blob/master/docs/source/images/test_coverage.png)


Explanation of the coverage report
----------------------------------
Eventhough we have configured Travis CI and CodeCov for this repository, we have decided to create a different test coverage report using on our local machine. The reasons why we have made this decision is due to the following:
There are certain functions that we cannot test using Espresso tests on Travis CI e.g. Adding a peer to your inbox, sending a block to a peer or sign a black that that you have received from another peer. This is difficult to test, as you would have connect with the bootstrap phone to find all other peers. 
There is no guarantee that you would always find the bootstrap phone during the test. Because of the dependency with the network and the network phone, we have decided run the espresso test locally and the unit tests are run on Travis.
The reason why the coverage is only at 62% is because there are a several classes that we could not test using unit test and Espresso test. This is due to the different dependencies that is needed to test the classes.


