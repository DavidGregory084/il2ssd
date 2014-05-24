(ns uk.org.il2ssd.event.bans
  (:require [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.ui :as ui]
            [uk.org.il2ssd.server :as server]))

(defn get-bans
  []
  (ui/clear-bans-data @state/control-instances)
  (server/get-bans))

(defn lift-ban
  []
  (let [{:keys [bans-table
                bans-data]} @state/control-instances
        index (ui/get-selected-index bans-table)]
    (when (>= index 0)
      (let [ban (ui/get-ban bans-data index)
            type (condp = (:type ban)
                   "Name" :name
                   "IP" :ip)
            value (:value ban)]
        (server/ban :rem type value)
        (get-bans)))))

(defn clear-bans
  []
  (server/ban :clear)
  (get-bans))