import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class TestNashorn {

   public void execute() throws ScriptException {

      String script = "";
      script += "print('testInstance=' + testInstance);";
      script += "print('class=' + Object.prototype.toString.call(testInstance));";
      script += "testInstance.testMethod('abcd');";

      ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
      TestClass testInstance = new TestClass();
      SimpleBindings global = new SimpleBindings();
      global.put("testInstance", testInstance);
      scriptEngine.eval(script, global);

   }

   public static void main(String[] args) throws ScriptException {
      TestNashorn testNashorn = new TestNashorn();
      testNashorn.execute();
   }

   public class TestClass {
      public void testMethod(String text) {
         System.out.println(text);
      };
   }
}
