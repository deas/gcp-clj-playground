(ns deas.gpp-job-metrics
  (:import (com.google.api Metric MonitoredResource)
           (com.google.cloud.monitoring.v3 MetricServiceClient)
           (com.google.monitoring.v3 CreateTimeSeriesRequest
                                     Point
                                     TimeInterval
                                     TimeSeries
                                     TypedValue)
           (com.google.protobuf.util Timestamps)
           (java.util ArrayList HashMap)))

(defn send-sample
  "I just a value to google monitoring"
  [project-id value]
  (let [interval (-> (TimeInterval/newBuilder)
                     (.setEndTime (Timestamps/fromMillis (System/currentTimeMillis)))
                     .build)
        value (-> (TypedValue/newBuilder)
                  (.setDoubleValue value))
        point (-> (Point/newBuilder)
                  (.setInterval interval)
                  (.setValue value)
                  .build)
        pointList (doto (ArrayList.)
                    (.add point))
        metricLabels (doto (HashMap.)
                       (.put "store_id" "PittsBurg"))
        metric (-> (Metric/newBuilder)
                   (.setType "custom.googleapis.com/stores/daily_sales")
                   (.putAllLabels metricLabels)
                   .build)
        resourceLabels (doto (HashMap.)
                         (.put "project_id" project-id))
        resource (-> (MonitoredResource/newBuilder)
                     (.setType "global")
                     (.putAllLabels resourceLabels)
                     .build)
        timeSeries (-> (TimeSeries/newBuilder)
                       (.setMetric metric)
                       (.setResource resource)
                       (.addAllPoints pointList)
                       .build)
        timeSeriesList (doto (ArrayList.)
                         (.add timeSeries))
        request (-> (CreateTimeSeriesRequest/newBuilder)
                    (.setName (str "projects/" project-id))
                    (.addAllTimeSeries timeSeriesList)
                    .build)]
    (with-open [client (MetricServiceClient/create)]
      (.createTimeSeries client request))))

;; https://cloud.google.com/monitoring/docs/reference/libraries
(comment
  (send-sample "your-project-id" 123.45)
  )


