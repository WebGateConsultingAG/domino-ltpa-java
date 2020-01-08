# Domino LTPA

This Library provides a simple way to generate, read and validate LTPA Tokens
for authentication with Domino.

This implementation only supports LTPA1 for Domino,
LTPA2 and/or LTPA for WebSphere is __NOT__ supported.

There is no publicly accessible definition for the LTPA format,
this implementation is based on a [blogpost](http://hasselba.ch/blog/?p=2525) by Sven Hasselbach.


## Usage

All actions related to LTPA are located in the class biz.webgate.domino.ltpa.Ltpa1.

Ltpa1 does not raise any Exceptions, but instead returns Optionals.

If you need to debug an issue, set `traceEnabled` to true to get printed stacktraces.

If you rather learn by example, the [test](./src/test/java/biz/webgate/domino/ltpa/test/TestLtpa1.java) is a good start.

### Configuration

The Ltpa1 instance has to be initialized with Configuration.

The interface biz.webgate.domino.ltpa.LtpaConfiguration provides a few default values ( no secrets :-O).

To configure your own secrets, extend LtpaConfiguration, i.e.:

	class MySecretConfguration implements LtpaConfiguration {
		@Override
		public Map<String, String> secrets(){
			return Collections.singletonMap("myDomain", "mySecret")		
		}
	}
	
Secrets are required to _validate_ and to _generate_ tokens.
Secrets are __not__ required for reading tokens.

#### Configuration variables

| name | type | description | default value |
| ---- | ---- | ----------- | ------------- |
| validMillis | int | how long a generated token will be valid | 5400 |
| graceMillis | int | grace duration for issue and expiry | 300 |
| secrets | Map<String, String>	| secrets by domain | empty |
| traceEnabled | boolean | prints error traces if true | false |

### Creating an Ltpa1 Instance

To work with LTPA-tokens, create an Ltpa1 instance with your Configuration:

	Ltpa1 ltpa = new Ltpa1(new MySecretConfiguration());

### Generate LTPA Token

	Optional<LtpaToken> token = ltpa.generate("cn=Alan Jones/ou=East/ou=Sales/o=Acme/c=us", "myDomain");
    token.map( LtpaToken::getToken).orElse("Token was not generated");
    
### Parse LTPA Token

_Does not validate the token signature! Use Ltpa1.validate._

	Optional<LtpaToken> token = ltpa.parse("AAECAzVlMTVkNWYwNWUxNWQ1ZjZQZXRlciBNYWZmYXlyZIx8gNVg6eyG4kFAsRIWRSxtTg==");
    token.map( LtpaToken::getUsername).orElse("Token could not be parsed");
    
### Validate LTPA Token
   
	boolean isValid = ltpa.validate("AAECAzVlMTVkNWYwNWUxNWQ1ZjZQZXRlciBNYWZmYXlyZIx8gNVg6eyG4kFAsRIWRSxtTg==", "myDomain");
    
    ltpa.parse("AAECAzVlMTVkNWYwNWUxNWQ1ZjZQZXRlciBNYWZmYXlyZIx8gNVg6eyG4kFAsRIWRSxtTg==")
    	.filter(ltpa::validate)
        .ifPresent( tkn -> System.out.println("token is valid");
        
TODO: LICENSE