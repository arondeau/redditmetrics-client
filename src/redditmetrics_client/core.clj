(ns redditmetrics-client.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [incanter.core :as incanter]
            [incanter.optimize :as optimize]
            [incanter.charts :as charts]
            [incanter.stats :as stats]))

(defn get-subreddit-http-response [subreddit-name]
  (client/post "http://redditmetrics.com/ajax/compare.reddits" 
               {:form-params {:reddit0 subreddit-name}}))

(defn get-json-response [http-response] 
  (clojure.walk/keywordize-keys (json/read-str (:body http-response))))

(defn get-data-series [json-response]  
  (->> json-response 
       :message 
       :growth 
       :data))

(defn get-timeseries-for-subreddit [subreddit-name]
  (get-data-series (get-json-response (get-subreddit-http-response subreddit-name))))

(defn make-scatter-plot-chart [X Y subreddit-name]
	  (charts/scatter-plot X Y :title subreddit-name :x-label "Days" :y-label "Subscribers"))

(defn ols-linear-model [Y X]
  			(stats/linear-model Y X))

(defn show-scatterplot-with-trendline-for-subreddit [subreddit-name day-count]
  (let [time-series (take-last day-count (get-timeseries-for-subreddit subreddit-name))
        X (range 0 (count time-series))
        Y (map #(:a %) time-series)]
    (incanter/view (charts/add-lines 
                    (make-scatter-plot-chart X Y (str "Subscriber Growth '" subreddit-name "' past " day-count " days"))
                    X (:fitted (ols-linear-model Y X))))))

;; usage 
;; (show-scatterplot-with-trendline-for-subreddit "DIY" 30)


(defn calculate-slope-degrees [X Y]
  (cond 
   (= 1 (count X)) (double 0.0)
   (= 0 (second X)) (double 90.0)
   :else (Math/toDegrees 
          (Math/atan (/ (- (second Y)
                           (first Y))
                        (- (second X)
                           (first X)))))))

(defn get-data-for-subreddit [name]
  (let [raw-response (client/post "http://redditmetrics.com/ajax/compare.reddits" {:form-params {:reddit0 name}})
        json-body (clojure.walk/keywordize-keys (json/read-str (:body raw-response)))
        data (->> json-body 
                  :message 
                  :growth 
                  :data)]
    (cond (>= 2  (count data)) '(() ())
          :else (let [x-range (range (count data))
                      lm (stats/linear-model x-range (map #(:a %) data))
                      y-range (lazy-seq (:fitted lm))]
                  (list x-range y-range)))))

(defn calculate-slope-degrees-for-subreddit [name days]
  (let [data (get-data-for-subreddit name)]
    (cond (>= 0 days) 0.0
          (= 0 (count (get-data-for-subreddit name))) 0.0
          :else (calculate-slope-degrees (take-last days  (first data)) (take-last days (second data))))))
