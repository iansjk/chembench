<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="sx" uri="/struts-dojo-tags" %> 
<%@ page language="java" import="java.util.*" %>

	<br />
	
	<!-- External Validation Compound Predictions -->
		<p class="StandardTextDarkGrayParagraph"><b><u>Predictions for External Validation Set</u></b></p>
	
	<s:if test="models.size==0">
		<s:if test="selectedPredictor.activityType=='CONTINUOUS'">
			<br/><b class="StandardTextDarkGray">No models that passed your r<sup>2</sup> and q<sup>2</sup> cutoffs were generated.</b><br/><br/>
		</s:if>
		<s:else>
			<b class="StandardTextDarkGray">No models were generated that passed your cutoffs.</b><br/><br/>
		</s:else>
	</s:if>
	<s:else>
	<table width="100%" align="center">
		<!--DWLayoutTable-->
		<tr>
		<td class="TableRowText01">Compound ID</td>
		<td class="TableRowText01">Structure</td>
		<td class="TableRowText01">Observed Value</td>
		<td class="TableRowText01">Predicted Value</td>
		<td class="TableRowText01">Residual</td>
		<td class="TableRowText01">Predicting Models / Total Models</td>
		</tr>
	</s:else>	
		
	<s:iterator value="externalValValues" status="extValStatus">
		<tr>
			<td class="TableRowText02"><s:property value="compoundId" /></td>
			<td class="TableRowText02">
			<a href="#" onclick="window.open('compound3D?project=<s:property value='selectedPredictor.name' />&projectType=modeling&compoundId=<s:property value='compoundId' />&user=<s:property value='user.userName' />&datasetID=<s:property value='selectedPredictor.datasetId' />', '<% new java.util.Date().getTime(); %>','width=350, height=350'); return false;">
			<img src="/imageServlet?projectType=modeling&user=<s:property value='user.userName' />&project=<s:property value='selectedPredictor.name' />&compoundId=<s:property value='compoundId' />&datasetID=<s:property value='selectedPredictor.datasetId' />" border="0" height="150" onmouseover='enlargeImage(this);' onmouseout='shrinkImage(this)'/>
			</a>
			</td>
			<td class="TableRowText02"><s:property value="actualValue" /></td>
			<td class="TableRowText02">
			<s:if test="numModels>=2">
				<s:property value="predictedValue" /> &#177; <s:property value="standDev" />
			</s:if>
			<s:elseif test="numModels==1">
				<s:property value="predictedValue" />
			</s:elseif>
			</td>
			<td class="TableRowText02">
				<s:property value="residuals[#extValStatus.index]" />
			</td>
			<td class="TableRowText02"><s:property value="numModels" /> / <s:property value="selectedPredictor.numTestModels" /></td>
		</tr>
	</s:iterator>
	
	</table>
	<br />
	<!-- End External Validation Compound Predictions -->
	
	<!-- External Validation Chart -->
	<s:if test="models.size!=0">
	<s:if test="dataType=='CONTINUOUS'">
		<p class="StandardTextDarkGrayParagraph"><u>External Validation Chart</u>
		<s:url id="externalChartLink" value="/externalValidationChart.do" includeParams="none">
			<s:param name="user" value="user.userName" />
			<s:param name="project" value="selectedPredictor.name" />
		</s:url>
		<br />
		<sx:div id="extValidationChart" href="%{externalChartLink}" theme="ajax">
		</sx:div>
		</p>
	</s:if>
	</s:if>
	<br />
	<!-- End External Validation Chart -->
	