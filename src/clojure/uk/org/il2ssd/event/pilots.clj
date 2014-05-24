(ns uk.org.il2ssd.event.pilots
  (:require [uk.org.il2ssd.state :as state]
            [uk.org.il2ssd.jfx.ui :as ui]
            [uk.org.il2ssd.server :as server]
            [clojure.string :as string]
            [uk.org.il2ssd.event.bans :as bans]))

(defn kick-pilot
  []
  (let [{:keys [pilots-table
                pilots-data]} @state/control-instances
        index (ui/get-selected-index pilots-table)]
    (when (>= index 0)
      (let [pilot (ui/get-pilot pilots-data index)
            number (:number pilot)]
        (server/kick :number number)))))

(defn ban-pilot
  []
  (let [{:keys [pilots-table
                pilots-data]} @state/control-instances
        index (ui/get-selected-index pilots-table)]
    (when (>= index 0)
      (let [pilot (ui/get-pilot pilots-data index)
            name (:name pilot)]
        (server/ban :add :name name)
        (bans/get-bans)))))

(defn ip-ban-pilot
  []
  (let [{:keys [pilots-table
                pilots-data]} @state/control-instances
        index (ui/get-selected-index pilots-table)]
    (when (>= index 0)
      (let [pilot (ui/get-pilot pilots-data index)
            ip (:ip pilot)]
        (server/ban :add :ip ip)
        (bans/get-bans)))))

(defn send-chat
  []
  (let [{:keys [pilots-table
                pilots-data
                chat-field]} @state/control-instances
        index (ui/get-selected-index pilots-table)]
    (when (>= index 0)
      (let [pilot (ui/get-pilot pilots-data index)
            number (:number pilot)
            message (ui/get-text chat-field)]
        (when-not (string/blank? message)
          (ui/clear-input chat-field)
          (server/chat :number number message))))))