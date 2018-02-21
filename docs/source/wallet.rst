************
Wallet
************

The wallet management works with a new `Tribler <https://www.tribler.org>`_ concept, called the reputation of each user. This reputation is based on the amount of data uploaded and downloaded, and the simple subtraction of these two quantities provides a number representing it. This number gives a positive value when the user is uploading more content than downloading, therefore contributing positively to the overall system. It will be referred in terms of tokens, that account for the reputation of the user, and can be transferred.

Import of tokens from PC
========================

This feature is done by generating a throw-away identity in the PC, to which a chosen amount of Tribler tokens are transferred. Then, this identity is exported to the phone by printing a QR code with the necessary information in the screen, and scanning it with the TrustChain Android app. To this mean, an option called "Import tokens" is implemented in the app's menu. Once both identities are settled in the phone, the final transfer is performed and only the phone identity outlasts.
Once this process has been completed, the phone has successfully received the tokens from Tribler and they can be checked in the "Funds" menu option. However it must be clarified that, as it is an offline process, there are no guarantees for preventing the double-spending problem.


Import/Export of tokens between phones
======================================

The functioning is exactly the same as the import of tokens PC-phone, only that now it is performed between two phones using the Trustchain Android app. In order to go through with it, the sender uses the menu option "Export tokens", which displays in the screen a QR code with all the necessary to export the total amount of tokens. Then, the receiver should make use of the "Import tokens" option to scan it and complete the transaction.
As it is an offline process, same security concerns as before apply here.

.. figure:: ./images/Import-export.png 
	:width: 600px

QR code
=======

The QR code has to be able to transfer the throw-away identity, while having a total size small enough to make it readable. To that effect, only the essential information is included in it: 

* Private key of the throw-away identity, which includes the private key special crypto format (explained in the :ref:`crypto-label` section).
* Transaction object, with the up and down quantities.
* Block hash and sequence number belonging to the half block of the transaction between the sender identity and the throw away one. 

This information is encoded in a JSON string before being put into the QR code. 

Once the QR code has been read, the receiver uses this information to reconstruct the transaction and throw-away identity.
The QR code used is the `version 13 <http://www.qrcode.com/en/about/version.html>`_, which has a capacity between 1440-3424 data bits depending on the ECC level.

Example
=======
An example of the data in the QR code is the following:

.. code-block:: json

	{
		"private_key": "TGliTmFDTFNLOvHyazzyYvb00cdAIb+xmDUzflFOsnzYTm3vbFcRV0FfuxWh827LrDLxYljG5+ga\n/m0SUkDYcDiHRnuf5BQ1HAI=\n", 
		"transaction": {"down": 0, "up": 11114175918}, 
		"block": {"block_hash": "7Jh0+S93fbtoqWwKQlYmsPMjC8eU7Bzo91NaKy/0d0w=\n", "sequence_number": 1}
	}


Which will result in the following QR code:

.. figure:: ./images/example-qr.jpg 
	:width: 300px

