SELECT D5.pFrom, D5.vFrom, D5.valFrom
FROM T2Provenance.DD D1 
join T2Provenance.DD D2 on
     D1.pfrom = D2.pTo and D1.vFrom = D2.vTo and D1.valFrom = D2.valTo
join DD D3 on
     D2.pFrom = D3.pTo and D2.vFrom = D3.vTo and D2.valFrom = D3.valTo
join DD D4 on
     D3.pFrom = D4.pTo and D3.vFrom = D4.vTo and D3.valFrom = D4.valTo
join DD D5 on
     D4.pFrom = D5.pTo and D4.vFrom = D5.vTo and D4.valFrom = D5.valTo
where D1.pTo = 'LINEARBLOCK_2' and D1.vTo = 'Y' and D1.iteration ='[0]'
