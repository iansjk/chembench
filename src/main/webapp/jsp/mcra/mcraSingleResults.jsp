<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>


<s:iterator value="mcraPredictions">
    <div>
        <dl class="dl-horizontal properties-list">
            <dt>Compound</dt>
            <dd><s:property value="name" /></dd>

            <dt>Predicted Activity</dt>
            <dd><s:property value="predictedActivity" /></dd>

            <dt>Nearest Neighbors</dt>
            <dd><s:property value="numNearestNeighbors" /></dd>
        </dl>

        <table class="table">
            <thead>
            <tr>
                <th>Descriptor Type</th>
                <th>Average Similarity</th>
                <th>Average Activity</th>
            </tr>
            </thead>

            <tbody>

            <s:iterator value="descriptors">
                <tr>
                    <td><span class="object-name"><s:property value="name" /></span></td>
                    <td><s:property value="averageSimilarity" /></td>
                    <td><s:property value="averageActivity" /></td>
                </tr>
            </s:iterator>

            </tbody>
        </table>
    </div>
</s:iterator>