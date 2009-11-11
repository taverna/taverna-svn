<%@ include file="/WEB-INF/jsp/header.jsp" %>
    <h2>Account Management</h2>
    <table>
    <tr><th>Username</th><th>Roles</th><th>Enabled</th></tr>
    <c:forEach items="${users}" var="user" varStatus="status">
      <tr class="r<c:out value="${status.count % 2}"/>">
      <td><c:out value="${user.username}"/></td>
      <td>
        <c:forEach items="${user.authorities}" var="authority">
          <c:out value="${authority}"/>
        </c:forEach>
      </td>
      <td><c:choose>
      <c:when test="${user.enabled}">yes</c:when>
      <c:otherwise>no</c:otherwise>
      </c:choose></td>
      <td>Edit</td>
      </tr>
    </c:forEach>
    </table>
<a href="<c:url value="adduser.html"/>">Add new user</a>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
