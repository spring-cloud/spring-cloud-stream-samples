<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<sf:form method="post" modelAttribute="person">

		<tr>
			<td class="control-label col-sm-2">Email:</td>
			<td><sf:input class="col-sm-10" path="email"
					name="name" type="text" /><br />
				<div class="error">
				</div></td>
	
		<tr>
		
			<tr>
			<td class="control-label col-sm-2">Name:</td>
			<td><sf:input class="col-sm-10" path="name"
					name="name" type="text" /><br />
				<div class="error">
				</div></td>
	
		<tr>
	<tr>
		<td class="label"></td>
		<td><input class="btn-primary pull-right" value="Create Person"
			type="submit" /></td>
	</tr>

</sf:form>
</body>
</html>