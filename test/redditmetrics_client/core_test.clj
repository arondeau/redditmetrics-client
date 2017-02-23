(ns redditmetrics-client.core-test
  (:require [clojure.test :refer :all]
            [redditmetrics-client.core :refer :all]))

(def X (range 10))
(def Y (range 10))
(def Y-with-intercept (range 1 11))
(def Y-random '(3 4 5 7 8 10 12 10 9 10))

(def timeseries [{:y "2017-02-16", :a 0}
                 {:y "2017-02-17", :a 1} 
                 {:y "2017-02-18", :a 2} 
                 {:y "2017-02-19", :a 3} 
                 {:y "2017-02-20", :a 4} 
                 {:y "2017-02-21", :a 5} 
                 {:y "2017-02-22", :a 6}
                 {:y "2017-02-22", :a 7}
                 {:y "2017-02-22", :a 8}
                 {:y "2017-02-22", :a 9}])

(deftest degrees-of-trendline-test
  (testing "when given uniform X Y starting at 0 0"
    (is (= 45 (int (degrees-of-trendline X Y)))))
  (testing "when given uniform X Y starting at 0 1"
    (is (= 45 (int (degrees-of-trendline X Y-with-intercept))))))

(deftest degrees-for-subreddit-test
  (with-redefs [get-timeseries-for-subreddit (fn [subreddit-name] timeseries)]
    (testing "when given a subreddit and day with intercept 0 0"
      (is (= 45 (int (degrees-for-subreddit "name" 10)))))))
