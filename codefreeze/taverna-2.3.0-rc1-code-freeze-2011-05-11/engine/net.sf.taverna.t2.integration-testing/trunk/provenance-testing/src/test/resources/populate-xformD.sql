insert into xformd (p, vto, valto, vfrom, valfrom, iteration)
(select pto, vto, valto, vfrom, valfrom, iteration
 from   DD 
 where pto = pfrom)

