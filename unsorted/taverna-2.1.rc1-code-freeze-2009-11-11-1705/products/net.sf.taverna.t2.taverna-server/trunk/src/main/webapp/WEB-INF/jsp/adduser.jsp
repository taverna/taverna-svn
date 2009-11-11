<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2>Add User</h2>

<form name="f" method="POST">
 <table width="95%" bgcolor="f8f8ff" border="0" cellspacing="0" cellpadding="5">
    <tr>
      <td alignment="right" width="20%">User Name:</td>
      <spring:bind path="adduser.userName">
        <td width="20%">
          <input type="text" name="userName" value="<c:out value="${status.value}"/>">
        </td>
        <td width="60%">
          <font color="red"><c:out value="${status.errorMessage}"/></font>
        </td>
      </spring:bind>
    </tr>
    <tr>
      <td alignment="right" width="20%">Password:</td>
      <spring:bind path="adduser.password">
        <td width="20%">
          <input type="password" name="password" value="<c:out value="${status.value}"/>">
        </td>
        <td width="60%">
          <font color="red"><c:out value="${status.errorMessage}"/></font>
        </td>
      </spring:bind>
    </tr>
    <tr>
      <td alignment="right" width="20%">Confirm Password:</td>
      <spring:bind path="adduser.passwordConfirmation">
        <td width="20%">
          <input type="password" name="passwordConfirmation" value="<c:out value="${status.value}"/>">
        </td>
        <td width="60%">
          <font color="red"><c:out value="${status.errorMessage}"/></font>
        </td>
      </spring:bind>
    </tr>
  </table>
  <br>
  <spring:hasBindErrors name="adduser">
    <b>Please fix all errors!</b>
  </spring:hasBindErrors>
  <input name="adduser" type="submit" alignment="center" value="Add User">
 </form>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
