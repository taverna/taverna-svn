<%@ include file="/WEB-INF/jsp/header.jsp" %>
    <h2>Jobs</h2>
    <table>
    <tr><th>Job ID</th><th>Created</th><th>Modified</th><th>Workflow</th><th>Inputs</th><th>Outputs</th><th>Status</th></tr>
    <c:forEach items="${jobs}" var="job" varStatus="status">
      <tr class="r<c:out value="${status.count % 2}"/>">
      <td><a href="<c:url value="rest/jobs/${job.id}"/>"><c:out value="${job.id}"/></a></td>
      <td><c:out value="${job.created}"/></td>
      <td><c:out value="${job.modified}"/></td>
      <td><a href="<c:url value="rest/workflows/${job.workflow}"/>"><c:out value="${job.workflow}"/></a></td>
      <c:choose>
      <c:when test="${job.inputs != null}"><td><a href="<c:url value="rest/data/${job.inputs}"/>"><c:out value="${job.inputs}"/></a></td></c:when>
      <c:otherwise><td>n/a</td></c:otherwise>
      </c:choose>
      <c:choose>
      <c:when test="${job.outputs != null}"><td><a href="<c:url value="rest/data/${job.outputs}"/>"><c:out value="${job.outputs}"/></a></td></c:when>
      <c:otherwise><td>n/a</td></c:otherwise>
      </c:choose>
      <td><c:out value="${job.status}"/></td>
      <td class="button">
        <form:form method="post" action="deletejob.html">
        <input type="hidden" name="id" value="<c:out value="${job.id}"/>" />
		<input type="submit" value="Delete"/>
	    </form:form>
	  </td>
      </tr>
    </c:forEach>
    </table>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
