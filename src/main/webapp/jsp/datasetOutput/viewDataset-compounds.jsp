<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<script language="javascript">

</script>

<!-- Compounds -->

<p class="StandardTextDarkGray"><u><b>All Compounds In Dataset</b></u></p>
<table width="924" align="center">
  <tr>
    <td>
      <p class="StandardTextDarkGray" width="550">Go To Page:
        <s:iterator value="pageNums" status="pageNumsStatus">
          <s:if test="pageNums[#pageNumsStatus.index]==currentPageNumber"><s:property /></s:if>
          <s:else><a href="#tabs" onclick=loadAllCompoundsTab("viewDatasetCompoundsSection?id=<s:property
              value='dataset.id' />&currentPageNumber=<s:property />&orderBy=<s:property
              value='orderBy' />&sortDirection=<s:property value='sortDirection' />")><s:property /></a></s:else>
        </s:iterator>
      </p>
    </td>
  </tr>
</table>
<table width="924" align="center">
  <tr>
    <!-- header for left side table -->
    <th class="TableRowText01">Compound ID<br />
      <a href="#tabs" onclick=loadAllCompoundsTab("viewDatasetCompoundsSection?id=<s:property
          value='dataset.id' />&currentPageNumber=<s:property
          value='currentPageNumber' />&orderBy=compoundId&sortDirection=asc")><img
          src="theme/img/sortArrowUp.png" /></a>
      <a href="#tabs" onclick=loadAllCompoundsTab("viewDatasetCompoundsSection?id=<s:property
          value='dataset.id' />&currentPageNumber=<s:property
          value='currentPageNumber' />&orderBy=compoundId&sortDirection=desc")><img src="theme/img/sortArrowDown.png" /></a>
    </th>
    <s:if test="!dataset.sdfFile.isEmpty()">
      <th class="TableRowText01_unsortable">Structure</th>
    </s:if>
    <s:if test="dataset.datasetType=='MODELING'||dataset.datasetType=='MODELINGWITHDESCRIPTORS'">
      <th class="TableRowText01">Activity<br />
        <a href="#tabs" onclick=loadAllCompoundsTab("viewDatasetCompoundsSection?id=<s:property
            value='dataset.id' />&currentPageNumber=<s:property
            value='currentPageNumber' />&orderBy=activityValue&sortDirection=asc")><img
            src="theme/img/sortArrowUp.png" /></a>
        <a href="#tabs" onclick=loadAllCompoundsTab("viewDatasetCompoundsSection?id=<s:property
            value='dataset.id' />&currentPageNumber=<s:property
            value='currentPageNumber' />&orderBy=activityValue&sortDirection=desc")><img
            src="theme/img/sortArrowDown.png" /></a>
      </th>
    </s:if>
  </tr>
  <!-- body for left side table -->
  <s:iterator value="datasetCompounds" status="datasetCompoundsStatus">
    <tr>
      <td class="TableRowText02"><s:property value="compoundId" /></td>
      <s:if test="!dataset.sdfFile.isEmpty()">
        <td class="TableRowText02">
          <a class="compound_img_a" href="#" onclick="window.open('compound3D?compoundId=<s:property
              value="url_friendly_id" />&projectType=dataset&user=<s:property value="dataset.userName" />&datasetName=
            <s:property value="dataset.name" />', '','width=350, height=350'); return false;">
            <img
                src="imageServlet?user=<s:property value="dataset.userName" />&projectType=dataset&compoundId=<s:property value='url_friendly_id' />&datasetName=<s:property value="dataset.name" />"
                border="0" height="150" />
          </a>
        </td>
      </s:if>
      <s:if test="dataset.datasetType=='MODELING'||dataset.datasetType=='MODELINGWITHDESCRIPTORS'">
        <td class="TableRowText02"><s:property value="activityValue" /></td>
      </s:if>
    </tr>
  </s:iterator>
</table>
<!-- End Compounds -->