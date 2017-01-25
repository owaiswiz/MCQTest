MCQTEST
======

Prerequisites -
----------------

1. A FTP Server Running with Write Access.You must enter the IP Address of this FTP Server before starting test.

2. Either make sure that there is a user "pi" with "raspberry" as password or change the credentials manually in InteractWithFTPServer Class's connectToFTPServer method.

3. Make sure that there is a "questions.txt" file present which contains the questions and options, along with the correct option.
   
   Format - 
   
   >__"question","option1","option2","option3","option4","correctOptionNumber"__
   
   Example -
   
   >__"Pointers in C are Given by","Star","Comma","Hyphen","Underscore","1"__
