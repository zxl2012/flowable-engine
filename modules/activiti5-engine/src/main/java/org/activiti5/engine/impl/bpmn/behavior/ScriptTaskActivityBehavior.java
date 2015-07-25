/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti5.engine.impl.bpmn.behavior;

import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.delegate.BpmnError;
import org.activiti5.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti5.engine.impl.scripting.ScriptingEngines;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * activity implementation of the BPMN 2.0 script task.
 * 
 * @author Joram Barrez
 * @author Christian Stettler
 * @author Falko Menge
 */
public class ScriptTaskActivityBehavior extends TaskActivityBehavior {
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(ScriptTaskActivityBehavior.class);
  
  protected String script;
  protected String language;
  protected String resultVariable;
  protected boolean storeScriptVariables = false; // see https://activiti.atlassian.net/browse/ACT-1626

  public ScriptTaskActivityBehavior(String script, String language, String resultVariable) {
    this.script = script;
    this.language = language;
    this.resultVariable = resultVariable;
  }
  
  public ScriptTaskActivityBehavior(String script, String language, String resultVariable, boolean storeScriptVariables) {
    this(script, language, resultVariable);
    this.storeScriptVariables = storeScriptVariables;
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    ScriptingEngines scriptingEngines = Context
      .getProcessEngineConfiguration()
      .getScriptingEngines();

    boolean noErrors = true;
    try {
      Object result = scriptingEngines.evaluate(script, language, execution, storeScriptVariables);
      
      if (resultVariable != null) {
        execution.setVariable(resultVariable, result);
      }

    } catch (ActivitiException e) {
      
      LOGGER.warn("Exception while executing " + execution.getActivity().getId() + " : " + e.getMessage());
      
      noErrors = false;
      Throwable rootCause = ExceptionUtils.getRootCause(e);
      if (rootCause instanceof BpmnError) {
        ErrorPropagation.propagateError((BpmnError) rootCause, execution);
      } else {
        throw e;
      }
    }
     if (noErrors) {
       leave(execution);
     }
  }
  
}
