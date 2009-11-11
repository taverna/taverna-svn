<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2>Rest Operations</h2>

<table>
<tr>
  <th>Operation</th>
  <th>Path</th>
  <th>Method</th>
  <th>Consumes</th>
  <th>Produces</th>
</tr>
<tr class=r1>
  <td>addData</td>
  <td>/data/</td>
  <td>POST</td>
  <td>application/xml</td>
  <td>text/plain</td>
</tr>
<tr class=r0>
  <td>addJob</td>
  <td>/jobs/</td>
  <td>POST</td>
  <td>application/xml</td>
  <td>text/plain</td>
</tr>
<tr class=r1>
  <td>addWorkflow</td>
  <td>/workflows/</td>
  <td>POST</td>
  <td>application/vnd.taverna.t2flow+xml</td>
  <td>text/plain</td>
</tr>
<tr class=r0>
  <td>deleteData</td>
  <td>/data/{id}</td>
  <td>DELETE</td>
  <td></td>
  <td></td>
</tr>
<tr class=r1>
  <td>deleteJob</td>
  <td>/jobs/{id}</td>
  <td>DELETE</td>
  <td></td>
  <td></td>
</tr>
<tr class=r0>
  <td>deleteWorkflow</td>
  <td>/workflows/{id}</td>
  <td>DELETE</td>
  <td></td>
  <td></td>
</tr>
<tr class=r1>
  <td>getData</td>
  <td>/data/{id}</td>
  <td>GET</td>
  <td></td>
  <td>application/xml</td>
</tr>
<tr class=r0>
  <td>getJob</td>
  <td>/jobs/{id}</td>
  <td>GET</td>
  <td></td>
  <td>application/xml</td>
</tr>
<tr class=r1>
  <td>getJobStatus</td>
  <td>/jobs/{id}/status</td>
  <td>GET</td>
  <td></td>
  <td>text/plain</td>
</tr>
<tr class=r0>
  <td>getOutput</td>
  <td>/jobs/{jobId}/{port}</td>
  <td>GET</td>
  <td></td>
  <td>?</td>
</tr>
<tr class=r1>
  <td>getWorkflow</td>
  <td>/workflows/{id}</td>
  <td>GET</td>
  <td></td>
  <td>application/vnd.taverna.t2flow+xml</td>
</tr>
<tr class=r0>
  <td>getWorkflows</td>
  <td>/workflows/</td>
  <td>GET</td>
  <td></td>
  <td>application/xml</td>
</tr>

</table>

<h2>Types</h2>

See <a href="<c:url value="schema1.xsd"/>">xml schema definition</a>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
