************
Crypto
************

The app maintains cryptographic compatibility with `Tribler <https://www.tribler.org>`_, using `Libsodium <https://download.libsodium.org/doc/>`_. To achieve this, `Java JNI bindings <https://github.com/joshjdevl/libsodium-jni>`_ has been used to allow the Trustchain Android app to use native libsodium method invocations. 

Libsodium has been chosen due to the fact that it is used by Tribler, and because it provides all of the core operations needed to build high-level cryptographic tools. It is a portable, cross-compilable, installable, packageable fork of `NaCL <http://nacl.cr.yp.to>`_ with a compatible extended API. Libsodium supports a variety of compilers and operating systems, including Windows (with MinGW or Visual Studio, x86 and x64), iOS, Android, as well as Javascript and Web Assembly. 

Libsodium supports the notion of `Dual Secrets <http://libnacl.readthedocs.io/en/latest/topics/dual.html>`_, an object that supports both encryption and signing. The key formats used in the app match the keys used in Triber, using ``xsalsa20poly1305`` for identity management and ``ed25519`` for signing. 

(De)serialization
==================================

During transmission and storage, keys are serialized and deserialized in the following manner:

* Public key pair: ``LibNaCLPk:`` + public key bytes + verify key bytes
* Private key pair: ``LibNaCLSk:`` + encryption seed + signing seed

The signing key is then generated using the signing seed. The private key can be generated from the encryption seed.
