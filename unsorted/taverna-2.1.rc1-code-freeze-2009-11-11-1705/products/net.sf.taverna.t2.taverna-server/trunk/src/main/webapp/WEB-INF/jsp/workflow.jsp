<%@ include file="/WEB-INF/jsp/header.jsp" %>
    <h2>Workflows</h2>
    <table>
    <tr><th>Workflow ID</th><th>Created</th><th>Modified</th><th>Enabled</th></tr>
    <c:forEach items="${workflows}" var="workflow" varStatus="status">
      <tr class="r<c:out value="${status.count % 2}"/>">
      <td><a href="<c:url value="rest/workflows/${workflow.id}"/>"><c:out value="${workflow.id}"/></a></td>
      <td><c:out value="${workflow.created}"/></td>
      <td><c:out value="${workflow.modified}"/></td>
      <td><c:out value="${workflow.enabled}"/></td>
      <td class="button">
      <c:choose>
      <c:when test="${workflow.enabled}">
        <form:form method="post" action="disableworkflow.html">
        <input type="hidden" name="id" value="<c:out value="${workflow.id}"/>" />
		<input type="submit" value="Disable"/>
	    </form:form>
      </c:when>
      <c:otherwise>
        <form:form method="post" action="enableworkflow.html">
        <input type="hidden" name="id" value="<c:out value="${workflow.id}"/>" />
		<input type="submit" value="Enable"/>
	    </form:form>
      </c:otherwise>
      </c:choose>
	  </td>
      <td class="button">
        <form:form method="post" action="deleteworkflow.html">
        <input type="hidden" name="id" value="<c:out value="${workflow.id}"/>" />
		<input type="submit" value="Delete"/>
	    </form:form>
	  </td>
      </tr>
    </c:forEach>
    </table>

	<h3>Upload a new workflow</h3>
	<form:form method="post" action="workflowupload.html" enctype="multipart/form-data">
 	  <input type="file" name="file" />
	  <input type="submit" value="Upload Workflow"/>
 	</form:form>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
