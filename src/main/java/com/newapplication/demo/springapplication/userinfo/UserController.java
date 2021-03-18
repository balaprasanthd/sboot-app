package com.newapplication.demo.springapplication.userinfo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.newapplication.demo.springapplication.UserApplication;

@RestController
public class UserController {

    @Autowired
    public IUserRepository repository;
    private CosmosClient client;

    private final String databaseName = "userinfo";
    private final String containerName = "userinfocontainer";

    private CosmosDatabase database;
    private CosmosContainer container;

    protected static Logger logger = LoggerFactory.getLogger(UserController.class);

    public void close() {
        client.close();
    }

    @GetMapping("/users/id/{id}")
    public void getUser(@PathVariable final String id) {
        this.startProcess();
    }

    /**
     * Run a Hello CosmosDB console application.
     * <p>
     * This sample is similar to SampleCRUDQuickstart, but modified to show indexing
     * capabilities of Cosmos DB. Look at the implementation of
     * createContainerIfNotExistsWithSpecifiedIndex() for the demonstration of
     * indexing capabilities.
     */
    // <Main>
    public static void startProcess() {

        UserController userController = new UserController();

        try {
            logger.info("Starting SYNC main");
            userController.indexManagementDemo();
            logger.info("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            logger.info("Closing the client");
            userController.shutdown();
        }
    }

    // </Main>

    private void indexManagementDemo() throws Exception {

        logger.info("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);

        ArrayList<String> preferredRegions = new ArrayList<String>();
        preferredRegions.add("West US");

        // Create sync client
        // <CreateSyncClient>
        client = new CosmosClientBuilder().endpoint(AccountSettings.HOST).key(AccountSettings.MASTER_KEY)
                .preferredRegions(preferredRegions).consistencyLevel(ConsistencyLevel.EVENTUAL)
                .contentResponseOnWriteEnabled(true).buildClient();

        // </CreateSyncClient>

        createDatabaseIfNotExists();

        // Here is where index management is performed
        createContainerIfNotExistsWithSpecifiedIndex();

        // Setup family items to create
        ArrayList<User> usersToCreate = new ArrayList<>();
        usersToCreate.add(Users.getFirstUser());
        usersToCreate.add(Users.getSecondUser());
        usersToCreate.add(Users.getThirdUser());
        usersToCreate.add(Users.getFourthUser());

        createUsers(usersToCreate);

        // logger.info("Reading items.");
        // readItems(usersToCreate);
        //
        logger.info("Querying items.");
        // queryItems();
    }

    private void createDatabaseIfNotExists() throws Exception {
        logger.info("Create database " + databaseName + " if not exists.");

        // Create database if not exists
        // <CreateDatabaseIfNotExists>
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(databaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());

        // </CreateDatabaseIfNotExists>

        logger.info("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExistsWithSpecifiedIndex() throws Exception {
        logger.info("Create container " + containerName + " if not exists.");

        // Create container if not exists
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, "/name");

        // <CustomIndexingPolicy>
        /*
         * IndexingPolicy indexingPolicy = new IndexingPolicy();
         * indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT); //To turn indexing
         * off set IndexingMode.NONE
         * 
         * // Included paths List<IncludedPath> includedPaths = new ArrayList<>();
         * includedPaths.add(new IncludedPath("/*"));
         * indexingPolicy.setIncludedPaths(includedPaths);
         * 
         * // Excluded paths List<ExcludedPath> excludedPaths = new ArrayList<>();
         * excludedPaths.add(new ExcludedPath("/name/*"));
         * indexingPolicy.setExcludedPaths(excludedPaths);
         */

        // Spatial indices - if you need them, here is how to set them up:
        /*
         * List<SpatialSpec> spatialIndexes = new ArrayList<SpatialSpec>();
         * List<SpatialType> collectionOfSpatialTypes = new ArrayList<SpatialType>();
         * 
         * SpatialSpec spec = new SpatialSpec(); spec.setPath("/locations/*");
         * collectionOfSpatialTypes.add(SpatialType.Point);
         * spec.setSpatialTypes(collectionOfSpatialTypes); spatialIndexes.add(spec);
         * 
         * indexingPolicy.setSpatialIndexes(spatialIndexes);
         */

        // Composite indices - if you need them, here is how to set them up:
        /*
         * List<List<CompositePath>> compositeIndexes = new ArrayList<>();
         * List<CompositePath> compositePaths = new ArrayList<>();
         * 
         * CompositePath nameCompositePath = new CompositePath();
         * nameCompositePath.setPath("/name");
         * nameCompositePath.setOrder(CompositePathSortOrder.ASCENDING);
         * 
         * CompositePath ageCompositePath = new CompositePath();
         * ageCompositePath.setPath("/age");
         * ageCompositePath.setOrder(CompositePathSortOrder.DESCENDING);
         * 
         * compositePaths.add(ageCompositePath); compositePaths.add(nameCompositePath);
         * 
         * compositeIndexes.add(compositePaths);
         * indexingPolicy.setCompositeIndexes(compositeIndexes);
         */

        /* containerProperties.setIndexingPolicy(indexingPolicy); */

        // </CustomIndexingPolicy>

        // Create container with 400 RU/s
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);
        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties,
                throughputProperties);
        container = database.getContainer(containerResponse.getProperties().getId());

        logger.info("Checking container " + container.getId() + " completed!\n");
    }

    private void createUsers(List<User> users) throws Exception {
        double totalRequestCharge = 0;
        for (User user : users) {

            // <CreateItem>
            // Create item using container that we created using sync client

            // Use lastName as partitionKey for cosmos item
            // Using appropriate partition key improves the performance of database
            // operations
            CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
            CosmosItemResponse<User> item = container.createItem(user, new PartitionKey(user.getName()),
                    cosmosItemRequestOptions);
            // </CreateItem>

            // Get request charge and other properties like latency, and diagnostics
            // strings, etc.
            logger.info(String.format("Created item with request charge of %.2f within" + " duration %s",
                    item.getRequestCharge(), item.getDuration()));
            totalRequestCharge += item.getRequestCharge();
        }
        logger.info(String.format("Created %d items with total request " + "charge of %.2f", users.size(),
                totalRequestCharge));
    }

    private void readItems(ArrayList<User> usersToCreate) {
        // Using partition key for point read scenarios.
        // This will help fast look up of items because of partition key
        usersToCreate.forEach(user -> {
            // <ReadItem>
            try {
                CosmosItemResponse<User> item = container.readItem(user.getId(), new PartitionKey(user.getName()),
                        User.class);
                // double requestCharge = item.getRequestCharge();
                // Duration requestLatency = item.getDuration();
                // logger.info(item.getItem().getName());
            } catch (CosmosException e) {
                e.printStackTrace();
                logger.error(String.format("Read Item failed with %s", e));
            }
            // </ReadItem>
        });
    }

    private void queryItems() {
        // <QueryItems>

        // Set some common query options
        int preferredPageSize = 10;
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        // Set populate query metrics to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);

        CosmosPagedIterable<User> userIterable = container.queryItems("SELECT * FROM userinfo", queryOptions,
                User.class);
        System.out.println(userIterable);
        userIterable.iterableByPage(preferredPageSize).forEach(cosmosItemPropertiesFeedResponse -> {
            logger.info("Got a page of query result with " + cosmosItemPropertiesFeedResponse.getResults().size()
                    + " items(s)" + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge());

            logger.info("Item Ids " + cosmosItemPropertiesFeedResponse.getResults().stream().map(User::getId)
                    .collect(Collectors.toList()));
        });
        // </QueryItems>
    }

    private void shutdown() {
        // try {
        // //Clean shutdown
        // logger.info("Deleting Cosmos DB resources");
        // logger.info("-Deleting container...");
        // if (container != null)
        // container.delete();
        // logger.info("-Deleting database...");
        // if (database != null)
        // database.delete();
        // logger.info("-Closing the client...");
        // } catch (Exception err) {
        // logger.error("Deleting Cosmos DB resources failed, will still attempt to
        // close the client. See stack trace below.");
        // err.printStackTrace();
        // }
        client.close();
        logger.info("Done.");
    }
}
