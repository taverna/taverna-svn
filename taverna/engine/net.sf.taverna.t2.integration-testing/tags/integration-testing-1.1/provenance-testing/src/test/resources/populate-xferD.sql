insert into xferd (pto, vto, valto, pfrom, vfrom, valfrom)
(select pto, vto, valto, pfrom, vfrom, valfrom
 from   DD 
 where pto <> pfrom)

