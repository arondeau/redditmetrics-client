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

(defn get-dataset-for-subreddit [subreddit-name]
  (map #(:a %) 
       (get-data-series (get-json-response (get-subreddit-http-response subreddit-name)))))

(defn make-scatter-plot-chart [X Y]
	  (charts/xy-plot X Y))

(defn ols-linear-model [Y X]
  			(stats/linear-model Y X))

(defn show-scatterplot-with-trendline-for-subreddit [subreddit-name day-count]
  (let [time-series (take-last day-count (get-timeseries-for-subreddit subreddit-name))
        X (range 0 (count time-series))
        Y (map #(:a %) time-series)]
    (incanter/view (charts/add-lines 
                    (make-scatter-plot-chart X Y)
                    X (:fitted (ols-linear-model Y X))))))


;; usage 
;; (show-scatterplot-with-trendline-for-subreddit "DIY" 30)
