package org.sustain.census;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sustain.census.db.Util;

import java.util.Iterator;

public class SpatialClient {
    private static final Logger log = LogManager.getLogger(SpatialClient.class);

    private CensusGrpc.CensusBlockingStub censusBlockingStub;

    public SpatialClient() {
        String target = Util.getProperty(Constants.Server.HOST) + ":" + Constants.Server.PORT;
        log.info("Target: " + target);

        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        censusBlockingStub = CensusGrpc.newBlockingStub(channel);
    }

    public CensusGrpc.CensusBlockingStub getCensusBlockingStub() {
        return censusBlockingStub;
    }

    public static void main(String[] args) {
        CensusGrpc.CensusBlockingStub censusBlockingStub = new SpatialClient().getCensusBlockingStub();
        final String geoJson = "{\n" +
                "   \"type\":\"Feature\",\n" +
                "   \"properties\":{\n" +
                "\n" +
                "   },\n" +
                "   \"geometry\":{\n" +
                "      \"type\":\"polygon\",\n" +
                "      \"coordinates\":[\n" +
                "  [\n" +
                "                                        [\n" +
                "                                        -105.72280883789064,\n" +
                "                                        40.390488829277956\n" +
                "                                        ],\n" +
                "                                        [\n" +
                "                                        -105.72280883789064,\n" +
                "                                        40.75661990450192\n" +
                "                                        ],\n" +
                "                                        [\n" +
                "                                        -104.44976806640626,\n" +
                "                                        40.75661990450192\n" +
                "                                        ],\n" +
                "                                        [\n" +
                "                                        -104.44976806640626,\n" +
                "                                        40.390488829277956\n" +
                "                                        ],\n" +
                "                                        [\n" +
                "                                        -105.72280883789064,\n" +
                "                                        40.390488829277956\n" +
                "                                        ]\n" +
                "                                    ]\n" +
                "      ]\n" +
                "   }\n" +
                "}";

        //exampleSpatialQuery(censusBlockingStub, geoJson);
        //exampleTargetedQuery(censusBlockingStub, geoJson);
        //exampleOsmQuery(censusBlockingStub, geoJson);
        exampleDatasetQuery(censusBlockingStub, geoJson);
    }

    private static void exampleDatasetQuery(CensusGrpc.CensusBlockingStub censusBlockingStub, String geoJson) {
        DatasetRequest request = DatasetRequest.newBuilder()
                .setDataset(DatasetRequest.Dataset.HOSPITALS)
                .setSpatialOp(SpatialOp.GeoWithin)
                .setRequestGeoJson(geoJson)
                .build();
        Iterator<DatasetResponse> datasetResponseIterator = censusBlockingStub.datasetQuery(request);
        int count = 0;
        while (datasetResponseIterator.hasNext()) {
            DatasetResponse response = datasetResponseIterator.next();
            count++;
            log.info(response.getResponse() + "\n");
        }

        log.info("Count: " + count);
    }

    private static void exampleOsmQuery(CensusGrpc.CensusBlockingStub censusBlockingStub, String geoJson) {
        OsmRequest request = OsmRequest.newBuilder()
                .setDataset(OsmRequest.Dataset.LINES)
                .setSpatialOp(SpatialOp.GeoIntersects)
                .putRequestParams("properties.highway", "primary")
                .setRequestGeoJson(geoJson).build();

        Iterator<OsmResponse> osmResponseIterator = censusBlockingStub.osmQuery(request);
        int count = 0;
        while (osmResponseIterator.hasNext()) {
            OsmResponse response = osmResponseIterator.next();
            count++;
            log.info(response.getResponse() + "\n");
        }

        log.info("Count: " + count);
    }

    private static void exampleSpatialQuery(CensusGrpc.CensusBlockingStub censusBlockingStub, String geoJson) {
        SpatialRequest request = SpatialRequest.newBuilder()
                .setCensusFeature(CensusFeature.Race)
                .setCensusResolution(CensusResolution.Tract)
                .setSpatialOp(SpatialOp.GeoWithin)
                .setRequestGeoJson(geoJson)
                .build();

        Iterator<SpatialResponse> spatialResponseIterator = censusBlockingStub.spatialQuery(request);
        while (spatialResponseIterator.hasNext()) {
            SpatialResponse response = spatialResponseIterator.next();
            String data = response.getData();
            String responseGeoJson = response.getResponseGeoJson();
            log.info("data: " + data);
            log.info("geoJson: " + responseGeoJson);
            System.out.println();
        }
    }

    private static void exampleTargetedQuery(CensusGrpc.CensusBlockingStub censusBlockingStub, String geoJson) {
        TargetedCensusRequest request = TargetedCensusRequest.newBuilder()
                .setResolution(CensusResolution.Tract)
                .setPredicate(
                        Predicate.newBuilder().setCensusFeature(CensusFeature.TotalPopulation)
                                .setComparisonOp(Predicate.ComparisonOperator.GREATER_THAN)
                                .setDecade(Decade._2010)
                                .setComparisonValue(2000)
                                .build()
                )
                .setSpatialOp(SpatialOp.GeoWithin)
                .setRequestGeoJson(geoJson)
                .build();

        Iterator<TargetedCensusResponse> censusResponseIterator =
                censusBlockingStub.executeTargetedCensusQuery(request);
        while (censusResponseIterator.hasNext()) {
            TargetedCensusResponse response = censusResponseIterator.next();
            String data = response.getSpatialResponse().getData();
            String responseGeoJson = response.getSpatialResponse().getResponseGeoJson();
            log.info("data: " + data);
            log.info("geoJson: " + responseGeoJson);
            System.out.println();
        }
    }
}