You can use cURL to post, get, put, delete bindings.

For example, to post a new binding use something like:

 curl -d "rdf=........add your own rdf here......" http://localhost:25000/sbs
 
 now, you can also post files using
 
 curl -F "rdf=@filename" http://localhost:25000/sbs  BUT i haven't got it to work yet
 
 when you succesfully post it returns the url for the binding which you can get by doing, for 
 example,
 curl  http://localhost:25000/sbs/cfcbcbac-4467-46dc-903f-e774042b70a
 
 notice that bindings are given a unique identifier using UUID