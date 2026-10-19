import java.util.function.Predicate;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

// Checks the GraalJS jars embedded in org.titou10.jtb.core the way VisualizersManager uses them:
// the engine is found via the ScriptEngine API, scripts are compiled once and run with host access to Java objects.
// Run by headless.sh with the Java runtime bundled with JMSToolBox and the jars of org.titou10.jtb.core/lib on the class path
public class GraalCheck {

   public static void main(String[] args) throws Exception {
      // Same settings as in VisualizersManager.initialize()
      ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("js");
      if (scriptEngine == null) {
         throw new IllegalStateException("No JavaScript engine found");
      }
      Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
      bindings.put("polyglot.js.allowHostAccess", true);
      bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) _ -> true);

      // A visualizer-like script: reads the payload, uses JavaScript features and calls back into a Java object
      String source = """
               var data = JSON.parse(jtb_payloadText);
               var upper = data.items.map(function (i) { return i.toUpperCase(); }).join(",");
               jtb_visualizer.add(upper);
               upper + "|" + java.lang.Integer.toHexString(data.count);
               """;
      CompiledScript cs = ((Compilable) scriptEngine).compile(source);

      StringBuilder sb = new StringBuilder();
      SimpleBindings global = new SimpleBindings();
      global.put("jtb_payloadText", "{\"items\":[\"a\",\"b\"],\"count\":255}");
      global.put("jtb_visualizer", new Hook(sb));
      Object result = cs.eval(global);

      String expected = "A,B|ff";
      if (!expected.equals(result) || !"A,B".equals(sb.toString())) {
         throw new IllegalStateException("Unexpected result '" + result + "' / '" + sb + "', expected '" + expected + "'");
      }
      System.out.println("GraalJS OK: " + scriptEngine.getFactory().getEngineName() + " " + scriptEngine.getFactory().getEngineVersion());
   }

   // Stands for VisualizerScriptsHook, the Java object scripts get as "jtb_visualizer"
   public static class Hook {
      private final StringBuilder sb;

      public Hook(StringBuilder sb) {
         this.sb = sb;
      }

      public void add(String s) {
         sb.append(s);
      }
   }
}
