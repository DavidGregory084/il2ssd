(ns uk.org.il2ssd.event.console
  (:require [uk.org.il2ssd.jfx.ui :as ui]
            [uk.org.il2ssd.server :as server]
            [uk.org.il2ssd.state :as state]))

(defn enter-command
  "### enter-command
   This is zero argument function which checks whether the text in the TextField
   control is \"clear\".

   In that case we clear the application's server console text. For all other text
   values, we send the entered text as a command to the server."
  []
  (let [{:keys [cmd-entry
                console]} @state/control-instances]
    (if (= (ui/get-text cmd-entry) "clear")
      (do (ui/clear-input console)
          (ui/clear-input cmd-entry))
      (do (server/write-socket (ui/get-text cmd-entry))
          (ui/clear-input cmd-entry)))))

