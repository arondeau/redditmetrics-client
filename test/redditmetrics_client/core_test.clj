(ns redditmetrics-client.core-test
  (:require [clojure.test :refer :all]
            [clj-http.client :as client]
            [redditmetrics-client.core :refer :all]))

(deftest calculate-slope-degrees-test
  (testing "given two sets of points representing the X and Y values on a two-dimensional space"
    (testing "when the dataset is too small"
      (is (= 0.0 (calculate-slope-degrees '(0) '(0)))))
    (testing "when the growth is 90 degrees"
      (is (= 90.0 (calculate-slope-degrees '(0 0) '(0 1)))))
    (testing "when y stays zero"
      (is (= 0.0 (calculate-slope-degrees '(0 1) '(0 0)))))
    (testing "when it grows positively by 45 degrees"
      (is (= 45.0 (calculate-slope-degrees '(0 1) '(0 1))))
      (testing "when the y-intercept is 1"
        (is (= 45.0 (calculate-slope-degrees '(0 1) '(1 2))))))
    (testing "when it decreases by 45 degrees"
      (is (= -45.0 (calculate-slope-degrees '(0 1) '(0 -1))))
      (testing "when the y-intercept is -1"
        (is (= -45.0 (calculate-slope-degrees '(0 1) '(-1 -2))))))
    (testing "when given known x/y relationships"
      (is (= 4.763641690726177 (calculate-slope-degrees '(0 36) '(0 3))))
      (is (= -4.763641690726177 (calculate-slope-degrees '(0 36) '(0 -3))))
      (is (= 1.1457628381751035 (calculate-slope-degrees '(0 50) '(0 1))))
      (is (= 26.56505117707799 (calculate-slope-degrees '(0 2) '(0 1)))))))

(deftest calculate-slope-degrees-for-subreddit-test
  (testing "given a subreddit name and time-frame in days"
    (testing "when there is no data for the subreddit"
      (is (= 0.0 (calculate-slope-degrees-for-subreddit "Unknown" 30))))
    (testing "when 0 days is given"
      (is (= 0.0 (calculate-slope-degrees-for-subreddit "Subreddit" 0))))
    (testing "when there is a simple data set"
      (testing "with data set #1"
        (with-redefs [get-data-for-subreddit (fn [subreddit-name] ['(0 1) '(0 1)])]
          (is (= 45.0 (calculate-slope-degrees-for-subreddit "Subreddit" 30)))))
      (testing "with data set #2"
        (with-redefs [get-data-for-subreddit (fn [subreddit-name] ['(0 36) '(0 3)])]
          (is (= 4.763641690726177 (calculate-slope-degrees-for-subreddit "Subreddit" 30))))))
    (testing "when there is more days in the data set than days requested"
        (with-redefs [get-data-for-subreddit (fn [subreddit-name] ['(0 1 2) '(0 10 20)])]
          (is (= 84.28940686250037 (calculate-slope-degrees-for-subreddit "Subreddit" 2)))))))

(def no-growth-data-body "{\"message\":{\"growth\": {\"data\" []}}}")
(def single-growth-data-point-body "{\"message\":{\"growth\": {\"data\" [{\"y\":\"2017-02-17\",\"a\":1}]}}}")
(def two-growth-data-points-body "{\"message\":{\"growth\": {\"data\" [{\"y\":\"2017-02-17\",\"a\":0}, {\"y\":\"2017-02-17\",\"a\":1}]}}}")
(def three-growth-data-points-body "{\"message\":{\"growth\": {\"data\" [{\"y\":\"2017-02-17\",\"a\":100}, {\"y\":\"2017-02-18\",\"a\":200},  {\"y\":\"2017-02-19\",\"a\":1000}]}}}")

(deftest get-data-for-subreddit-test
  (testing "given a subreddit name and time-frame in days"
    (testing "when there is no data for the subreddit"
      (with-redefs [client/post (fn [url & more] {:body no-growth-data-body})]
        (is (= '(() ()) (get-data-for-subreddit "Unknown")))))
    (testing "when there is only a single data point"
      (with-redefs [client/post (fn [url & more] {:body single-growth-data-point-body})]
        (is (= '(() ()) (get-data-for-subreddit "SingleDayReddit")))))
    (testing "when there are two data points"
      (with-redefs [client/post (fn [url & more] {:body two-growth-data-points-body})]
        (is (= '((0 1) '(0 1)))) (get-data-for-subreddit "TwoDayReddit")))
    (testing "when there are three data points"
      (with-redefs [client/post (fn [url & more] {:body three-growth-data-points-body})]
        (is (= '((0 1 2) (0.38356164383561653 0.5684931506849316 2.0479452054794516)) (get-data-for-subreddit "TwoDayReddit")))))))
