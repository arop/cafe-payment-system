-- Function: public.updatemenuversion()

DROP FUNCTION public.updatemenuversion();

CREATE OR REPLACE FUNCTION public.updatemenuversion()
  RETURNS trigger AS
$BODY$
BEGIN 
	UPDATE globals 
	SET value = round(date_part( 'epoch', now())*1000) 
	WHERE key = 'menu_version';
	return new;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.updatemenuversion()
  OWNER TO ***REMOVED***;


CREATE TRIGGER updateMenuVersionTrigger 
AFTER INSERT OR UPDATE OR DELETE ON products
FOR EACH ROW EXECUTE PROCEDURE updateMenuVersion();