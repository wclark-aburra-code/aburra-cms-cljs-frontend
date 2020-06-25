(ns sortable-portlets2.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]            
            ))
(def endpt "https://o4r2xvobjc.execute-api.us-east-1.amazonaws.com/dev/hello")
(def authkey (let [qstring (-> js/window .-location .-search)
;; ERR HANDLING!!

]

(last (re-find #"\?Auth\=(.*)" qstring))
) )
(defn sleep [ms f]
                                        (js/setTimeout f ms)
  ;(f)
  )
(defn scroll! [tag] (set! (.-hash js/window.location) tag))
;(def authkey "eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIn0.L06g5LB2SK39pjnko0utGqz1Xk_fgVSZn31zP-MPGHgxOZZ2wyZ5EZuU_sMrseukRkVJh0tlrmkM8tgKFvm6DCfH9PaqTXQD.3I9TLTCG4NOot9R33voTrA.b8h7N7UiRMJpJbnpai1habVExfdAYHAXrrWq7Y_s4mUle02FR6RnyZJS70M8w4_CJ68dw9qGctxLiKYvLwwqTezwZ8zhWBm5-TPf-08kOhY.9DJxszNDfevjd_vyuafpLky2klLFfy6CqUuG0afEnTo")

                                        ;(def postendpt "https://nd6tmnhtu6.execute-api.us-east-1.amazonaws.com/dev/submit")
(def postendpt "https://o4r2xvobjc.execute-api.us-east-1.amazonaws.com/dev/submit")
(defn make-hsh [coll] (reduce (fn [hsh item]  (assoc hsh (nth item 0) (nth item 1)))  {} (map vector (map #(str "k" %) (range 0 (count coll))) coll)))
(defn hash-as-vec [hsh] (into [] hsh))
                                        ;{"EventLink":"https://vk.com/shahmen_mockba","EventLinkIsWindow":true,"Month":"October","Day":18,"City":"Moscow, Russia","Club":"Moskva","TicketLink":"https://tele-club.ru/moskvaclub/shahmen-msk?utm_source=vk&utm_medium=post&utm_campaign=shahmen-msk&utm_term=anton-shibanov&utm_content=promoanons","TicketLinkIsWindow":true,"VipLink":"https://www.eventbrite.co.uk/e/shahmen-vip-meet-greet-moscow-tickets-69300773479","VipLinkIsWindow":true}
                                        ; ticket-link TicketLink
                                        ; vip-link VipLink
                                        ; club Club
                                        ; city City
                                        ; instead of Date -- Month, Day
                                        ; *NEW* param - EventLink
                                        ; these are set to true by default: TicketLinkIsWindow, VipLinkIsWindow, EventLinkIsWindow


                                        ;(def citiez-db (reagent/atom (make-hsh citiez)))
(def citiez-db (reagent/atom {}))
(def orig-keys (reagent/atom '()))
(def current-tour (reagent/atom "Bless Tour"))
;(def class-atom (reagent/atom {:first "base-class"}))

(def counter (atom 0)) ; not reagent atom? difference?
(defn next-key [] (do  (swap! counter inc) (str "kn" @counter) )) ; how we generate new keyz

(def edit-state (reagent/atom {:EventLink nil :TicketLinkIsWindow true :EventLinkIsWindow true :VipLinkIsWindow true :active false :City nil :Club nil :Day nil :Month nil :TicketLink nil :TourName @current-tour :VipLink nil  :editing {:Club nil :City nil :id nil :record {} }  } ) )

(def to-delete (reagent/atom ()))

                                        ; *each item* in listing, incl. "start editing" button
(defn date-str [evt-record]
(str (:Month evt-record) " " (:Day evt-record))
  )
(defn item-com [evt-list-item]
  (let [ evt-record (nth evt-list-item 1) hsh-id (nth evt-list-item 0)  pk-value (:EventLink evt-record)]
    [:div {:class "evt-record"}
     [:div (date-str evt-record)]
     [:a {:href (:EventLink evt-record)} "Event Link"]
     [:div {:class "divider"}]
     [:a {:href (:TicketLink evt-record)} "Ticket Link"]
     [:div {:class "divider"}]
     [:a {:href (:VipLink evt-record)} "VIP Link"]
     [:li [:div [:span {:style {:font-weight "700"}} (date-str evt-record)] [:span (:City evt-record)] [:span (:Club evt-record)]   ]]
[:button { :class "list-ctrl"
         :on-click #((do (swap! edit-state assoc  :TicketLinkIsWindow (:TicketLinkIsWindow evt-record) :VipLinkIsWindow (:VipLinkIsWindow evt-record) :EventLinkIsWindow (:EventLinkIsWindow evt-record)     :EventLink (:EventLink evt-record) :TourName @current-tour :City (:City evt-record) :Club (:Club evt-record) :Month (:Month evt-record) :Day (:Day evt-record) :TicketLink (:TicketLink evt-record) :VipLink (:VipLink evt-record)  :editing {:id hsh-id :record evt-record} :active true :Club (:Club evt-record) :City (:City evt-record))  (scroll! "#editcomponent") )) }  "Edit" ]
   [:div {:class "divider"}]  [:button {
           :class "list-ctrl"   :on-click
                                        #((do
                                            (swap! to-delete conj pk-value  )
                                          (swap! citiez-db dissoc hsh-id)  )
                                          )
              } "Erase"]
     ;  #(swap! citiez-db assoc hsh-id nil) ; this almost works, but setting to nil is whack because it leaves blank entries
     ])
  )

(defn log [& args] (apply (.-log js/console) args))



(defn load-tour []
  (go
    (let [response (<! (http/get (str endpt "?TourName=" @current-tour)
                                 {:with-credentials? false
                                  :headers {"Authorization" authkey}
;                                  :headers {"Origin" "http://201.185.217.225"
;                                            "Access-Control-Request-Headers" "Origin"
;                                            "Access-Control-Request-Method" "GET"
                                        ;                                           }
}))]
                   ;              }))]
(do      (reset! citiez-db  (make-hsh (:Dates (:body response))))
         (reset! orig-keys (map (fn [x] (:EventLink  x) ) (:Dates (:body response))) )
;         (scroll! "#starting")
        ))))



(defn shows-lister [items]
[:div
;  [:div {:class (str "nueva-boton " (:first @class-atom))}
 [:div {:class "float-left slct-section" :id "starting"}
  [:select {:class "select-css" :on-change #((do
                                              (reset! current-tour (.. % -target -value))
                                              (load-tour)
                                              (swap! edit-state assoc :active false)

                                              )) }   ;; WE NEED A "LOAD" ACTION also called in beginning
   ;; style it, make a list that dynamically populates the options, and empty/null safe if none
 [:option {:value "Bless Tour"} "Bless Tour"]  
 [:option {:value "CH Shows"} "CH Shows"]]

  ][:div {:class "float-clear"}]
 [:div {:class "los-botones"}

 [:div {:class "nueva-boton padded-top"}
   [:button {:class "nuevo-btn" :on-click #(( let [nu  (next-key)] (do

                                                                     ( swap! citiez-db assoc nu {:City nil :TourName @current-tour :Club nil :TicketLink nil :VipLink nil :EventLinkIsWindow true :VipLinkIsWindow true :TicketLinkIsWindow true :Month nil :Day nil :EventLink nil})
                                                                     (swap! edit-state assoc :active true :editing {:id  nu} :City nil :Club nil :EventLinkIsWindow true :TicketLinkIsWindow true :VipLinkIsWindow true :TourName @current-tour :TicketLink nil :EventLink nil :VipLink nil :Month nil :Day nil)
                                                                     (scroll! "#editcomponent")
                                                                     ) )  ) } "Add a new tour date."]]
 [:div {:class "divider float-left"}]
  [:div {:class "post-boton"}
   [:button {:class "submit-btn padded-top" :on-click #(do
                                                         (http/post postendpt {:json-params {:orig-keys @orig-keys  :dates (vals @citiez-db) :tour-name @current-tour :to-delete @to-delete} :headers {"Authorization" authkey}  :with-credentials? false })

                                                             (http/post "https://o4r2xvobjc.execute-api.us-east-1.amazonaws.com/dev/template" {
                                                                                      :with-credentials? false
                                                                                      :json-params {:TourName @current-tour}
                                                                                      :headers {"Authorization" authkey}
                                                                                      })
                                              )
} "POST"]
  ] [:div {:class "float-clear"}]]
 
[:ul
   (for [item (hash-as-vec items)]
     ^{:key item} (item-com item))]  ])
(defn scrollstart [] (scroll! "#starting"))
;; **EDIT** component
(defn edit-component []


  [:div {:id "editcomponent"  :class  (if (:active @edit-state) "editing" "nah")}
;   (if (:active @edit-state)
     (let [tmp (:record (:editing @edit-state))]
       [:div

        
    [:div {:class "input-bubble"}    [:input {:type "text" :value (:City @edit-state)  :on-change #(

                                        (swap! edit-state assoc :City (-> % .-target .-value)))
                                                                     
                 } ] [:span {:class "lbl"} "City"] ]

        

        [:div {:class "input-bubble"}
         [:input {:type "text" :value (:EventLink @edit-state) :on-change #((swap! edit-state assoc :EventLink (-> % .-target .-value))   )} ]  [:span {:class "lbl"} "Event Link"] [:div {:class "float-right"} [:input {:type "checkbox" :id "e-link" :checked (:EventLinkIsWindow @edit-state) :on-change #((swap! edit-state update :EventLinkIsWindow not)) }] [:label {:for "e-link" :class "open-lbl"} "Open in new window"]]]
        

        [:div {:class "input-bubble"}        [:input {:type "text" :value (:VipLink @edit-state) :on-change #((swap! edit-state assoc :VipLink (-> % .-target .-value))   )} ]  [:span {:class "lbl"} "VIP Link"]
         [:div {:class "float-right"} [:input {:type "checkbox" :checked (:VipLinkIsWindow @edit-state) :on-change      #((swap! edit-state update :VipLinkIsWindow not ) )  :id "vip-link"}] [:label
                                                                                    {:for "vip-link"  :class "open-lbl"} "Open in new window"]]
         ]


        [:div {:class "input-bubble"}  [:input {:type "text" :value (:TicketLink @edit-state) :on-change #((swap! edit-state assoc :TicketLink (-> % .-target .-value)))}]
[:span {:class "lbl"} "Ticket Link"] [:div {:class "float-right"} [:input {:type "checkbox"  :id "tix-link" :checked (:TicketLinkIsWindow @edit-state) :on-change #((swap! edit-state update :TicketLinkIsWindow not))}] [:label {:for "tix-link"  :class "open-lbl"} "Open in new window"]]]
                                                                                                                                                                                                                     
        
        [:div {:class "input-bubble"}
         [:input {:type "text" :value (:Club @edit-state) :on-change #((swap! edit-state assoc :Club (-> % .-target .-value))   )}][:span {:class "lbl"} "Club"] ] [:div {:class "input-bubble"}   [:input {:type "text" :value (:Month @edit-state) :on-change #((swap! edit-state assoc :Month (-> % .-target .-value)))}] [:span {:class "lbl"} "Month"] ]  [:div {:class "input-bubble"}    [:input {:type "text" :value (:Day @edit-state) :on-change #((swap! edit-state assoc :Day (-> % .-target .-value))) }]  [:span {:class "lbl"} "Day"]]
         
        
        [:div {:class "float-clear"}]
        [:div {:class "botones-edit"}
        [:button { :class "edit-ctrl"
                :on-click
#(do ( swap! citiez-db assoc (:id (:editing @edit-state)) {:EventLinkIsWindow (:EventLinkIsWindow @edit-state) :VipLinkIsWindow (:VipLinkIsWindow @edit-state) :TicketLinkIsWindow (:TicketLinkIsWindow @edit-state)  :EventLink (:EventLink @edit-state)    :City (:City @edit-state) :Club (:Club @edit-state) :TourName @current-tour :Day (:Day @edit-state) :Month (:Month @edit-state)  :VipLink (:VipLink @edit-state) :TicketLink (:TicketLink @edit-state)} ) (swap! edit-state assoc :active false ) (scroll! "#starting") )
                  ;(sleep scrollstart 2000)
                 } "Ok"]  [:div {:class "divider"}] [:button {:class "edit-ctrl"   :on-click #( (do

                                                                                                  (swap! edit-state assoc :active false)  (scroll! "#starting")
                                                                                                  )    ) } "CXL"]]  ] )
   ])

(defn lister-concerts []
  [:div
   [edit-component]
   [:br]
   [shows-lister  @citiez-db]])

(defn ^:export main []
  (do
(reagent/render [lister-concerts]
                 (.getElementById js/document "app"))
(scroll! "#starting")
(load-tour)

    )
  )
