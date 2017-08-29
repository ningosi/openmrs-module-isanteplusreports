package org.openmrs.module.isanteplusreports.page.controller;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.isanteplusreports.IsantePlusReportsProperties;
import org.openmrs.module.isanteplusreports.definitions.ArvReportManager;
import org.openmrs.module.isanteplusreports.healthqual.builder.HealthQualHtmlTableBuilder;
import org.openmrs.module.isanteplusreports.model.HealthQualIndicator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthQualReportPageController {
	
	private final Log log = LogFactory.getLog(getClass());
	
	IsantePlusReportsProperties props = new IsantePlusReportsProperties();
	
	public void get(@SpringBean ArvReportManager reportManager,
	        @RequestParam(required = false, value = "startDate") Date startDate,
	        @RequestParam(required = false, value = "endDate") Date endDate, PageModel model) throws IOException {
		
		if (startDate == null) {
			startDate = DateUtils.addDays(new Date(), -21);
		}
		if (endDate == null) {
			endDate = new Date();
		}
		startDate = DateUtil.getStartOfDay(startDate);
		endDate = DateUtil.getEndOfDay(endDate);
		
		model.addAttribute("reportManager", reportManager);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("divWithResult", null);
	}
	
	public void post(@SpringBean ArvReportManager reportManager,
	        @SpringBean ReportDefinitionService reportDefinitionService,
	        @RequestParam(value = "indicatorList") List<HealthQualIndicator> indicators,
	        @RequestParam(required = false, value = "startDate") Date startDate,
	        @RequestParam(required = false, value = "endDate") Date endDate, PageModel model) throws IOException,
	        EvaluationException {
		
		if (startDate == null) {
			startDate = DateUtils.addDays(new Date(), -21);
		}
		if (endDate == null) {
			endDate = new Date();
		}
		
		startDate = DateUtil.getStartOfDay(startDate);
		endDate = DateUtil.getEndOfDay(endDate);
		
		HealthQualHtmlTableBuilder builder = new HealthQualHtmlTableBuilder();
		for (HealthQualIndicator indicator : indicators) {
			
			ReportDefinition reportDefinition = reportDefinitionService.getDefinitionByUuid(indicator.getUuid());
			Map<String, Object> parameterValues = new HashMap<String, Object>();
			if (indicator.getOption() != null) {
				parameterValues.putAll(indicator.getOption());
			}
			for (Parameter parameter : reportDefinition.getParameters()) {
				if (parameter.getName().equals("startDate")) {
					parameterValues.put("startDate", startDate);
				}
				if (parameter.getName().equals("endDate")) {
					parameterValues.put("endDate", endDate);
				}
			}
			
			EvaluationContext context = new EvaluationContext();
			context.setParameterValues(parameterValues);
			
			try {
				builder.addReportData(reportDefinitionService.evaluate(reportDefinition, context));
			}
			catch (EvaluationException e) {
				log.error("Evaluation exception was thrown");
				throw e;
			}
			
		}
		
		model.addAttribute("reportManager", reportManager);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("divWithResult", builder.build());
	}
}
