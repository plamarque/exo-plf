<%@page import="org.exoplatform.trial.TrialService"%>
<center><a href="/trial/UnlockServlet?rdate=<%=TrialService.computeRemindDateFromTodayBase64()%>"  class="Button StartEvaluationButton">Full use of the product for 30 days period</a></center>