<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="sx" uri="/struts-dojo-tags" %> 
<%@page language="java" import="java.util.*" %>

<html>
<head>
<title>C-CHEMBENCH | Freely Available Software</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="icon"  href="theme/img/mml.ico" type="image/ico"></link>
<link rel="SHORTCUT ICON" href="theme/img/mml.ico" ></link>
<link href="/theme/ccbTheme/css/ccbStyle.css" rel="stylesheet" type="text/css" />
<script language="JavaScript" src="javascript/script.js"> </script>
 
</head>
<body onload="setTabToHome();">
<table width="924" border="0" align="center" cellpadding="0" cellspacing="0"><tr><td><%@include file="/jsp/main/header.jsp" %></td></tr></table>
<table width="924" border="0" align="center" cellpadding="0" cellspacing="0"><tr><td><%@include file="/jsp/main/centralNavigationBar.jsp" %></td></tr></table>
<br />

 	<table width="924" frame="border" rules="none" align="center" cellpadding="0" cellspacing="4">
		<tbody>			
		<tr>
			<td height="24" align="left">
			<p class="StandardTextDarkGrayParagraph2">
			<br /><b>Free Cheminformatics Tools</b>
			</p></td>
		</tr>
		<tr>
			<td>
			<div class="StandardTextDarkGrayParagraph"><i><!-- space for a description if needed --></i><br /></div></td>
		 </tr>	
		 
		<!-- Table of software and links -->
		<tr>
			<td class="TableRowText01">Name</td>
			<!-- <td class="TableRowText01">Type</td> -->
			<td class="TableRowText01">Function</td>
			<td class="TableRowText01">Availability</td>
			<td class="TableRowText01">Reference</td>
			<!-- allow admins to delete bad entries. -->
			<s:if test="userIsAdmin"><td class="TableRowText01">Delete</td></s:if>
		</tr>
		<s:iterator value="softwareLinks">
			<tr>
			<td class="TableRowText02"><a href="<s:property value="url" />"><s:property value="name" /></a></td>
			<!-- <td class="TableRowText02">type</td> -->
			<td class="TableRowText02"><s:property value="function" /></td>
			<td class="TableRowText02"><s:property value="availability" /></td>
			<td class="TableRowText02"><s:property value="reference" /></td>
			<s:if test="userIsAdmin">
			<td class="TableRowText02"><a href="deleteSoftwareLink?id=<s:property value="id" />">Delete</a></td>
			</s:if>
			</tr> 
		</s:iterator>
		<tr><td colspan="2">&nbsp;</td></tr>
	</table>

	<br /><br />
	<!-- Add a Tool -->
	<s:form action="addSoftware" enctype="multipart/form-data" theme="simple">
	<table width="924" frame="border" rules="none" align="center" cellpadding="0" cellspacing="4" colspan="2">
			<tbody>
				<tr>
					<td align="left" colspan="2">
					<div class="StandardTextDarkGrayParagraph2" align="left"><b>Add New Tool</b></div><br />
					</td>
			    </tr> 
				<tr>
				<td>
				
				<s:if test="userName!=''">
				<!-- only allow logged in users to do this -->
				<table>
				<tr>
					<td height="26">
					<div align="right" class="StandardTextDarkGray"><b>Select a Tool Type: </b></div>
					</td>
					<td align="left" valign="top">
					
					</td>
				</tr>		
				<tr>
					<td height="26">
					<div align="right" class="StandardTextDarkGray"><b>Name: </b></div>
					</td>
					<td align="left" valign="top"><s:textfield name="name" id="name" size="40" /><span id="messageDiv2"></span></td>
				</tr>
				<tr>
					<td height="26">
					<div align="right" class="StandardTextDarkGray"><b>URL: </b></div>
					</td>
					<td align="left" valign="top"><s:textfield name="url" id="url" size="40" /><span id="messageDiv2"></span></td>
				</tr>
				<tr>
					<td height="26">
					<div align="right" class="StandardTextDarkGray"><b>Function: </b></div>
					</td>
					<td align="left" valign="top"><s:textfield name="function" id="function" size="40" /><span id="messageDiv2"></span></td>
				</tr>
				<tr>
					<td height="26">
					<div align="right" class="StandardTextDarkGray"><b>&nbsp;</b></div>
					</td>
					<td align="left" valign="top">
					<input type="button" name="userAction" id="userAction" onclick="this.submit();" value="Submit" />
					</td>
				</tr>
				</table>
				</s:if>
				
				<s:else>
				<!-- User isn't logged in; make them do that first. -->
				<tr>
					<td align="left" colspan="2">
					<div class="StandardTextDarkGrayParagraph" align="left"><i>To add a tool to the list, you must <a href="home.do">log in</a> first.</i></div><br />
					</td>
			    </tr> 
				</s:else>	
				
				</td></tr>
			</tbody>
		</table>
	</s:form>

</div></td></tr></tbody></table>

<%@include file ="/jsp/main/footer.jsp" %>
</body>
</html>