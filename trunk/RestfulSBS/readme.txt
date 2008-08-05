You can use the supplied SOGSAClient to do all the magic for you or you can use something 
like cURL to post, get, put, delete bindings.

For example, to post a new binding use something like:

 curl -d "rdf=........add your own rdf here......&entityKey=abcde" http://localhost:25000/sbs
 
 now, you can also post files using
 
 curl -F "rdf=@filename&entityKey=abcde" http://localhost:25000/sbs  BUT i haven't got it to work yet
 
 when you succesfully post it returns the url for the binding which you can get by doing, for 
 example,
 curl  http://localhost:25000/sbs/abcde
 
 notice that bindings are given a unique identifier based on what you supplied as the "entityKey"
 
 REMEMBER - change the org.openanzo.repository.database.url=jdbc:derby:/Users/Ian/scratch/anzoDerby;create=true;upgrade=true
 to point to the correct database (not mine!!)