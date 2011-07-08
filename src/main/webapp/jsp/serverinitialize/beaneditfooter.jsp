<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<button name="navigation" value="previous" class="wide">&lt; Previous</button>&nbsp;<button name="navigation" value="next" class="wide">Next &gt;</button>
<input type="hidden" name="step" value="${stepToShow.name}"/>

</form>