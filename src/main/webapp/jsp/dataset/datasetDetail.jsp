<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>
  <%@ include file="/jsp/main/head.jsp" %>
  <title>Chembench | Dataset: <s:property value="dataset.name" /></title>
</head>
<body>
<div id="main" class="container">
  <%@ include file="/jsp/main/header.jsp" %>

  <section id="content">
    <h2>Dataset Details: <s:property value="dataset.name" /></h2>

    <s:hidden id="username" value="%{dataset.userName}" />
    <s:hidden id="dataset-name" value="%{dataset.name}" />
    <s:hidden id="has-structures" value="%{dataset.hasStructures()}" />
    <s:hidden id="dataset-id" value="%{dataset.id}" />

    <div class="list-group">
      <div class="list-group-item">
        <h4 class="list-group-item-heading">General information</h4>
        <dl class="dl-horizontal properties-list">
          <dt>Dataset type</dt>
          <dd>
            <div class="dataset-type">
              <s:property value="dataset.datasetType" />
            </div>
          </dd>
          <dt>Number of compounds</dt>

          <dd>
            <s:property value="dataset.numCompound" />
          </dd>

          <dt>Activity type</dt>
          <dd>
            <s:if test="!dataset.hasActivities()">
              None
            </s:if>
            <s:else>
              <div class="activity-type">
                <s:property value="dataset.modelType" />
              </div>
            </s:else>
          </dd>

          <s:if test="dataset.canGenerateModi()">
            <dt>Modelability index</dt>
            <dd>
              <span class="modi-value"><s:property value="dataset.modi" /></span>
              <a class="modi-help" tabindex="0" role="button"><span
                  class="glyphicon glyphicon-question-sign"></span></a>
            </dd>
          </s:if>

          <dt>Date created</dt>
          <dd>
            <s:date name="dataset.createdTime" format="yyyy-MM-dd HH:mm" />
          </dd>
        </dl>
      </div>

      <div class="list-group-item">
        <h4 class="list-group-item-heading">Descriptors</h4>
        <dl class="dl-horizontal properties-list">
          <dt>Descriptors available</dt>
          <dd class="available-descriptors">
            <s:property value="dataset.availableDescriptors" />
          </dd>
          <s:if test="!dataset.uploadedDescriptorType.isEmpty()">
            <dt>Uploaded descriptor type</dt>
            <dd><s:property value="dataset.uploadedDescriptorType" /></dd>
          </s:if>
        </dl>
      </div>

      <div class="list-group-item">
        <h4 class="list-group-item-heading">
          Description and paper reference
          <s:if test="editable">
            <button id="edit-description-reference" class="btn btn-primary btn-xs">
              <span class="glyphicon glyphicon-pencil"></span> Edit
            </button>
          </s:if>
            <span id="description-reference-buttons">
              <button id="cancel-changes" class="btn btn-default btn-xs">
                <span class="glyphicon glyphicon-remove"></span> Cancel
              </button>
              <button id="save-changes" type="submit" class="btn btn-primary btn-xs">
                <span class="glyphicon glyphicon-floppy-disk"></span> Save
              </button>
            </span>
        </h4>
        <dl id="description-reference-text" class="properties-list">
          <dt>Description</dt>
          <dd id="description-wrapper">
            <div class="value"><s:property value="dataset.description" /></div>
            <div class="placeholder">
              <span class="text-muted">(No description given.)</span>
            </div>
          </dd>

          <dt>Paper reference</dt>
          <dd id="paper-reference-wrapper">
            <div class="value"><s:property value="dataset.paperReference" /></div>
            <div class="placeholder">
              <span class="text-muted">(No paper reference given.)</span>
            </div>
          </dd>
        </dl>

        <s:if test="editable">
          <s:form id="object-form" action="datasetDetail" method="POST" theme="simple">
            <div class="form-group">
              <label for="description">Description:</label>
              <s:textarea id="description" name="description" value="%{dataset.description}" cssClass="form-control" />
            </div>

            <div class="form-group">
              <label for="paper-reference">Paper reference:</label>
              <s:textarea id="paper-reference" name="paperReference" value="%{dataset.paperReference}"
                          cssClass="form-control" />
            </div>

            <s:hidden name="id" value="%{id}" />
          </s:form>
        </s:if>
      </div>
    </div>

    <ul class="nav nav-tabs">
      <li class="active"><a href="#all-compounds" data-toggle="tab">All Compounds</a></li>
      <s:if test="dataset.hasActivities()">
        <s:if test="dataset.splitType.equals(@edu.unc.ceccr.chembench.global.Constants@NFOLD)">
          <li><a href="#folds" data-toggle="tab">Folds</a></li>
        </s:if>
        <s:else>
          <li><a href="#external-set" data-toggle="tab">External Set</a></li>
        </s:else>
        <li><a href="#activity-histogram" data-toggle="tab">Activity Histogram</a></li>
      </s:if>
      <s:if test="dataset.hasStructures()">
        <li><a href="#descriptors" data-toggle="tab">Descriptors</a></li>
        <li><a href="#heatmap" data-toggle="tab">Heatmap</a></li>
      </s:if>
    </ul>

    <div class="tab-content">
      <div id="all-compounds" class="tab-pane active">
        <h3>All Compounds</h3>

        <p class="margin-below">All compounds in your dataset are listed in the table below, including those in
          your external set.
        </p>
        <table class="table table-hover table-bordered compound-list datatable">
          <thead>
          <tr>
            <th class="name">Compound Name</th>
            <%--<s:if test="dataset.hasStructures()">
              <th class="unsortable">Structure</th>
            </s:if>--%>
            <s:if test="dataset.hasActivities()">
              <th>Activity</th>
            </s:if>
          </tr>
          </thead>
          <tbody>
          <s:iterator value="datasetCompounds">
            <tr>
              <td class="name compound-structure"><s:property value="compoundId"/></td>
              <%--<s:if test="!dataset.sdfFile.isEmpty()">
                <td class="structure">
                  <s:url var="imageUrl" action="imageServlet" escapeAmp="false">
                    <s:param name="user" value="%{dataset.userName}" />
                    <s:param name="projectType" value="'dataset'" />
                    <s:param name="compoundId" value="%{compoundId}" />
                    <s:param name="datasetName" value="%{dataset.name}" />
                  </s:url>
                  <button class="btn btn-default">
                    <img src="<s:property value="imageUrl" />" class="img-thumbnail compound-structure"
                         width="125" height="125" alt="Compound structure">
                  </button>
                </td>
              </s:if>--%>
              <s:if test="dataset.hasActivities()">
                <td><s:property value="activityValue" /></td>
              </s:if>
            </tr>
          </s:iterator>
          </tbody>
        </table>
      </div>

      <s:if test="dataset.hasActivities()">
        <s:if test="dataset.splitType.equals(@edu.unc.ceccr.chembench.global.Constants@NFOLD)">
          <div id="folds" class="tab-pane">
            <h3>Folds</h3>

            <p>The compounds in each fold of your dataset are listed below. Use the fold navigation buttons to
              switch between folds.
            </p>
            <nav class="text-center">
              <ul class="pagination">
                <s:iterator value="foldNumbers" status="s">
                  <s:url var="foldUrl" action="viewDatasetFold">
                    <s:param name="id" value="dataset.id" />
                    <s:param name="foldNumber" value="%{#s.count}" />
                  </s:url>
                  <s:if test="%{#s.first}">
                    <li class="first-fold active"><s:a href="%{foldUrl}">
                      <s:property />
                    </s:a></li>
                  </s:if>
                  <s:elseif test="%{#s.last}">
                    <li class="last-fold"><s:a href="%{foldUrl}">
                      <s:property />
                    </s:a></li>
                  </s:elseif>
                  <s:else>
                    <li><s:a href="%{foldUrl}">
                      <s:property />
                    </s:a></li>
                  </s:else>
                </s:iterator>
              </ul>
            </nav>

            <table class="table table-hover compound-list datatable">
              <thead>
              <tr>
                <th>Compound Name</th>
                <%--<s:if test="dataset.hasStructures()">
                  <th class="unsortable">Structure</th>
                </s:if>--%>
                <s:if test="dataset.hasActivities()">
                  <th>Activity</th>
                </s:if>
              </tr>
              </thead>
              <tbody></tbody>
            </table>
          </div>
        </s:if>
        <s:else>
          <div id="external-set" class="tab-pane">
            <h3>External Set</h3>

            <p class="margin-below">The compounds in your dataset's external test set are listed in the table
              below.
            </p>
            <table class="table table-hover table-bordered compound-list datatable">
              <thead>
              <tr>
                <th>Compound Name</th>
                <%--<s:if test="dataset.hasStructures()">
                  <th class="unsortable">Structure</th>
                </s:if>--%>
                <s:if test="dataset.hasActivities()">
                  <th>Activity</th>
                </s:if>
              </tr>
              </thead>
              <tbody>
              <s:iterator value="externalCompounds">
                <tr>
                  <td class="name"><s:property value="compoundId" /></td>
                  <%--<s:if test="!dataset.sdfFile.isEmpty()">
                    <td><s:url var="imageUrl" action="imageServlet" escapeAmp="false">
                      <s:param name="user" value="%{dataset.userName}" />
                      <s:param name="projectType" value="'dataset'" />
                      <s:param name="compoundId" value="%{compoundId}" />
                      <s:param name="datasetName" value="%{dataset.name}" />
                    </s:url><img src="<s:property value="imageUrl" />" class="img-thumbnail compound-structure"
                                 width="125" height="125" alt="Compound structure"></td>
                  </s:if>--%>
                  <s:if test="dataset.hasActivities()">
                    <td><s:property value="activityValue" /></td>
                  </s:if>
                </tr>
              </s:iterator>
              </tbody>
            </table>
          </div>
        </s:else>

        <div id="activity-histogram" class="tab-pane">
          <h3>Activity Histogram</h3>

          <p class="margin-below">Below is a histogram of the activity values of the compounds in your dataset.
            The range of activity values have been divided into 10 bins.
          </p>

          <div class="text-center">
            <s:url var="activityChartUrl" action="imageServlet" escapeAmp="false">
              <s:param name="user" value="%{dataset.userName}" />
              <s:param name="projectType" value="'dataset'" />
              <s:param name="project" value="%{dataset.name}" />
              <s:param name="compoundId" value="'activityChart'" />
            </s:url>
            <img src="<s:property value="activityChartUrl" />" width="550" height="550" alt="Activity histogram">
          </div>
        </div>
      </s:if>

      <div id="descriptors" class="tab-pane">
        <h3>Descriptors</h3>

        <p class="margin-below">Here you can see the results of descriptor generation for each descriptor type,
          and a summary of any errors that occurred during descriptor generation.
        </p>

        <s:iterator value="descriptorGenerationResults">
          <dl class="dl-horizontal">
            <dt>
              <s:property value="descriptorType" />
            </dt>
            <dd>
              <s:property value="generationResult" />
            </dd>

            <s:if test="!generationResult.equalsIgnoreCase('successful')">
              <dt>Error Summary</dt>
              <dd>
                <s:property value="programOutput" />
              </dd>

            </s:if>
          </dl>
        </s:iterator>
      </div>

      <s:if test="dataset.datasetType.startsWith(@edu.unc.ceccr.chembench.global.Constants@MODELING)">
        <div id="heatmap" class="tab-pane">
          <h3>Heatmap</h3>

          <p>
            The <b>Heatmap</b> tool is useful for visualizing similarity between all pairs of compounds in your dataset.
            MACCS keys are generated and compared to produce the heatmap. The similarity is based on Tanimoto similarity
            or Mahalanobis distance; you can select either using the buttons on the Heatmap.
          </p>

          <p>
            You can zoom in and out of the heatmap using the mouse wheel. Click and drag the top of a column to shift it
            left or right. You may also drag rows up and down. To reset the rows and columns to their original
            configuration, use the <b>Reset</b> button.
          </p>

          <p>
            The <b>Keep Diagonal</b> button will shift rows and columns at the same time. Just below <b>Keep
            Diagonal</b> are four arrow buttons. These four arrows sort the rows / columns based on the activity values
            of
            the compounds. You can see the activity of each compound by hovering the mouse over a compound ID. The
            activity of each compound is also represented by the blue shading under each compound; darker blues indicate
            lower activity values.
          </p>

          <p>
            <span class="text-danger"><b>Note:</b></span> For very large datasets (500 or more compounds), the heatmap
            generation step will be skipped.
          </p>

          <hr>
          <div id="heatmapSwfWrapper" class="embed-responsive">
            <div id="heatmapSwfContainer">
              <div class="text-danger">
                <h4>Adobe Flash Required</h4>

                <p>The Heatmap tool requires Adobe Flash Player. Please install Flash to use the Heatmap tool.</p>
              </div>
            </div>
          </div>
        </div>
      </s:if>
    </div>
  </section>

  <%@ include file="/jsp/main/footer.jsp" %>
</div>

<%@ include file="/jsp/main/tail.jsp" %>
<script>
  $(document).ready(function() {
    window.datasetHasStructures = '<s:property value="dataset.hasStructures()" />';
    Chembench.Heatmap = {};
    Chembench.Heatmap.flashvars = {
      "web-addr": "<s:property value='webAddress' />",
      "dataset": "<s:property value='dataset.name' />",
      "ncom": "<s:property value='dataset.numCompound' />",
      "type_": "<s:property value='dataset.modelType' />",
      "creation_date": "<s:property value='dataset.createdTime' />",
      "desc": "<s:property value='dataset.description' />",
      "actFile": "<s:property value='dataset.actFile' />",
      "sdfFile": "<s:property value='dataset.sdfFile' />",
      "user": "<s:property value='dataset.userName' />"
    };
    Chembench.Heatmap.params = {
      "quality": "high"
    };
    Chembench.Heatmap.attributes = {
      "class": "embed-responsive-item"
    };
  });
</script>
<script src="${pageContext.request.contextPath}/assets/js/swfobject.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/autolink.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/editable.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/ChemDoodleWeb.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/compound-detail.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/datasetDetail.js"></script>
</body>
</html>
